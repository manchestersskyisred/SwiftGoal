package com.swiftgoal.app.controller;

import com.swiftgoal.app.service.DataImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping("/import-data")
    public ResponseEntity<Map<String, Object>> importData() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "开始导入2025赛季数据...");
            
            // 在后台线程中执行导入，避免阻塞HTTP响应
            new Thread(() -> {
                try {
                    dataImportService.importLeaguesTeamsAndPlayers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "导入失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 