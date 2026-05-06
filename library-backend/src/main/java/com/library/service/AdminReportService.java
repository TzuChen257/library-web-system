package com.library.service;

public interface AdminReportService {

    /**
     * 產生年度借閱統計 Excel
     *
     * @param year 年度
     * @param topN 排行榜筆數
     * @return Excel byte[]
     */
    byte[] generateBorrowStatisticsReport(int year, int topN);
}