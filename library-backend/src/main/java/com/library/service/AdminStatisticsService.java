package com.library.service;

import java.util.Map;

public interface AdminStatisticsService {
	//管理員後台統計摘要
    Map<String, Object> getSummary(Integer year, Integer month);
}
