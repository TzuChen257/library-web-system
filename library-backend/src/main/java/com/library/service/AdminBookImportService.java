package com.library.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface AdminBookImportService {

    /**
     * 產生書目與館藏匯入範本 Excel
     */
    byte[] generateBookImportTemplate();

    /**
     * 匯入書目與館藏 Excel
     */
    Map<String, Object> importBooks(MultipartFile file);
}