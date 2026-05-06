package com.library.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
	//首頁公開統計資料
	Map<String, Object> getPublicSummary();
	//首頁本月熱門借閱書籍
    List<Map<String, Object>> getPublicTopBorrowedBooks(Integer limit);
}
