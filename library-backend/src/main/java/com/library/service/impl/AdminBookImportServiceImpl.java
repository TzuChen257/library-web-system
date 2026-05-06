package com.library.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCategoryRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.AdminBookImportService;
import com.library.util.IdGenerator;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminBookImportServiceImpl implements AdminBookImportService {
	
	private BookRepository bookRepository;
    private BookCategoryRepository bookCategoryRepository;
    private BookCopyRepository bookCopyRepository;
	
    @Override
    public byte[] generateBookImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createTemplateSheet(workbook);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new LibraryBusinessException(ResponseCode.SYSTEM_ERROR);
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> importBooks(MultipartFile file) {
        LoginUserHolder.requireAdmin();

        if (file == null || file.isEmpty()) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "請選擇要匯入的 Excel 檔案");
        }

        List<Map<String, Object>> errors = new ArrayList<>();

        int totalRows = 0;
        int successCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }

                totalRows++;

                try {
                    importOneRow(row, formatter);
                    successCount++;
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("rowNumber", rowIndex + 1);
                    error.put("message", e.getMessage());
                    errors.add(error);
                }
            }

        } catch (IOException e) {
            throw new LibraryBusinessException(ResponseCode.SYSTEM_ERROR, "Excel 檔案讀取失敗");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", totalRows);
        result.put("successCount", successCount);
        result.put("failCount", errors.size());
        result.put("errors", errors);

        return result;
    }

    private void createTemplateSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("書目館藏匯入範本");

        createHeader(sheet);
        createExampleRows(sheet);
        autoSize(sheet, 9);
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("isbn");
        header.createCell(1).setCellValue("title");
        header.createCell(2).setCellValue("author");
        header.createCell(3).setCellValue("publisher");
        header.createCell(4).setCellValue("publishYear");
        header.createCell(5).setCellValue("categoryId");
        header.createCell(6).setCellValue("copyCode");
        header.createCell(7).setCellValue("location");
        header.createCell(8).setCellValue("note");
    }

    private void createExampleRows(Sheet sheet) {
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("9789863120000");
        row1.createCell(1).setCellValue("Java 入門");
        row1.createCell(2).setCellValue("王小明");
        row1.createCell(3).setCellValue("iRead 出版");
        row1.createCell(4).setCellValue(2026);
        row1.createCell(5).setCellValue("CAT001");
        row1.createCell(6).setCellValue("B00000001");
        row1.createCell(7).setCellValue("A區1櫃");
        row1.createCell(8).setCellValue("第一本館藏");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("9789863120000");
        row2.createCell(1).setCellValue("Java 入門");
        row2.createCell(2).setCellValue("王小明");
        row2.createCell(3).setCellValue("iRead 出版");
        row2.createCell(4).setCellValue(2026);
        row2.createCell(5).setCellValue("CAT001");
        row2.createCell(6).setCellValue("B00000002");
        row2.createCell(7).setCellValue("A區1櫃");
        row2.createCell(8).setCellValue("第二本館藏");
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    //單列匯入邏輯
    private void importOneRow(Row row, DataFormatter formatter) {
        String isbn = getCellString(row, 0, formatter);
        String title = getCellString(row, 1, formatter);
        String author = getCellString(row, 2, formatter);
        String publisher = getCellString(row, 3, formatter);
        String publishYearText = getCellString(row, 4, formatter);
        String categoryId = getCellString(row, 5, formatter);
        String copyCode = getCellString(row, 6, formatter);
        String location = getCellString(row, 7, formatter);
        String note = getCellString(row, 8, formatter);

        validateImportRow(title, categoryId, copyCode);

        if (bookCopyRepository.existsByCopyCode(copyCode)) {
            throw new LibraryBusinessException(
                    ResponseCode.BAD_REQUEST,
                    "館藏條碼已存在：" + copyCode
            );
        }

        BookCategory category = bookCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new LibraryBusinessException(
                        ResponseCode.CATEGORY_NOT_FOUND,
                        "找不到分類：" + categoryId
                ));

        Book book = findOrCreateBook(
                isbn,
                title,
                author,
                publisher,
                parsePublishYear(publishYearText),
                category
        );

        BookCopy copy = new BookCopy();
        copy.setCopyId(IdGenerator.generateBookCopyId());
        copy.setBook(book);
        copy.setCopyCode(copyCode);
        copy.setLocation(trimToNull(location));
        copy.setNote(trimToNull(note));
        copy.setCopyStatus(BookCopyStatus.AVAILABLE);

        bookCopyRepository.save(copy);
    }
    
    private Book findOrCreateBook(
            String isbn,
            String title,
            String author,
            String publisher,
            Integer publishYear,
            BookCategory category) {

        if (StringUtils.hasText(isbn)) {
            return bookRepository.findByIsbn(isbn.trim())
                    .orElseGet(() -> createBook(
                            isbn,
                            title,
                            author,
                            publisher,
                            publishYear,
                            category
                    ));
        }

        return createBook(
                null,
                title,
                author,
                publisher,
                publishYear,
                category
        );
    }

    private Book createBook(
            String isbn,
            String title,
            String author,
            String publisher,
            Integer publishYear,
            BookCategory category) {

        Book book = new Book();

        book.setBookId(IdGenerator.generateBookId());
        book.setCategory(category);
        book.setIsbn(trimToNull(isbn));
        book.setTitle(title.trim());
        book.setAuthor(trimToNull(author));
        book.setPublisher(trimToNull(publisher));
        book.setPublishYear(publishYear);
        book.setStatus(StatusType.ACTIVE);

        return bookRepository.save(book);
    }
    //補驗證與工具方法
    private void validateImportRow(String title, String categoryId, String copyCode) {
        if (!StringUtils.hasText(title)) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "書名不可為空");
        }

        if (!StringUtils.hasText(categoryId)) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "分類不可為空");
        }

        if (!StringUtils.hasText(copyCode)) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "館藏條碼不可為空");
        }
    }

    private Integer parsePublishYear(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "出版年份格式錯誤：" + value);
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int i = 0; i <= 8; i++) {
            if (StringUtils.hasText(getCellString(row, i, formatter))) {
                return false;
            }
        }

        return true;
    }

    private String getCellString(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}