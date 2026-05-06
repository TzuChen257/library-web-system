package com.library.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BorrowRecordRepository;
import com.library.service.AdminReportService;

@Service
public class AdminReportServiceImpl implements AdminReportService {
	
	@Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Override
    public byte[] generateBorrowStatisticsReport(int year, int topN) {
        int safeTopN = topN <= 0 ? 10 : topN;
        safeTopN = Math.min(safeTopN, 50);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createMonthlyBorrowSheet(workbook, year);
            createTopBorrowedBooksSheet(workbook, year, safeTopN);
            createTopReadersSheet(workbook, year, safeTopN);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new LibraryBusinessException(ResponseCode.SYSTEM_ERROR);
        }
    }

    /**
     * Sheet 1：年度每月借閱數
     */
    private void createMonthlyBorrowSheet(Workbook workbook, int year) {
        Sheet sheet = workbook.createSheet("年度每月借閱統計");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("月份");
        header.createCell(1).setCellValue("借閱數");

        List<Object[]> rows = borrowRecordRepository.countMonthlyBorrowsByYear(year);

        int rowIndex = 1;

        for (Object[] item : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(toInt(item[0]));
            row.createCell(1).setCellValue(toLong(item[1]));
        }

        autoSize(sheet, 2);
    }

    /**
     * Sheet 2：年度熱門書籍 Top N
     */
    private void createTopBorrowedBooksSheet(Workbook workbook, int year, int topN) {
        Sheet sheet = workbook.createSheet("熱門書籍排行");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("排名");
        header.createCell(1).setCellValue("書目ID");
        header.createCell(2).setCellValue("書名");
        header.createCell(3).setCellValue("作者");
        header.createCell(4).setCellValue("借閱次數");

        Pageable pageable = PageRequest.of(0, topN);

        List<Object[]> rows = borrowRecordRepository.findTopBorrowedBooksByYear(year, pageable);

        int rowIndex = 1;
        int rank = 1;

        for (Object[] item : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(toString(item[0]));
            row.createCell(2).setCellValue(toString(item[1]));
            row.createCell(3).setCellValue(toString(item[2]));
            row.createCell(4).setCellValue(toLong(item[3]));
        }

        autoSize(sheet, 5);
    }

    /**
     * Sheet 3：年度讀者借閱排行 Top N
     */
    private void createTopReadersSheet(Workbook workbook, int year, int topN) {
        Sheet sheet = workbook.createSheet("讀者借閱排行");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("排名");
        header.createCell(1).setCellValue("使用者ID");
        header.createCell(2).setCellValue("帳號");
        header.createCell(3).setCellValue("姓名");
        header.createCell(4).setCellValue("借閱次數");

        Pageable pageable = PageRequest.of(0, topN);

        List<Object[]> rows = borrowRecordRepository.findTopReadersByYear(year, pageable);

        int rowIndex = 1;
        int rank = 1;

        for (Object[] item : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(rank++);
            row.createCell(1).setCellValue(toString(item[0]));
            row.createCell(2).setCellValue(toString(item[1]));
            row.createCell(3).setCellValue(toString(item[2]));
            row.createCell(4).setCellValue(toLong(item[3]));
        }

        autoSize(sheet, 5);
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String toString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return Integer.parseInt(String.valueOf(value));
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }
}