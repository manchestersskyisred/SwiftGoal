package com.swiftgoal.app.command;

import com.swiftgoal.app.service.DataImportService;
import com.swiftgoal.app.service.ChineseNameTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.mode", havingValue = "data-import")
public class DataImportCommand implements CommandLineRunner {

    private final DataImportService dataImportService;
    private final ChineseNameTranslationService translationService;

    public DataImportCommand(DataImportService dataImportService, 
                           ChineseNameTranslationService translationService) {
        this.dataImportService = dataImportService;
        this.translationService = translationService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 开始数据导入模式 ===");
        
        try {
            // 1. 导入联赛、球队和球员数据
            log.info("步骤1: 开始导入联赛、球队和球员数据...");
            dataImportService.importLeaguesTeamsAndPlayers();
            log.info("✓ 联赛、球队和球员数据导入完成");
            
            // 2. 执行中文名翻译
            log.info("步骤2: 开始执行中文名翻译...");
            translationService.translateAllNames();
            log.info("✓ 中文名翻译完成");
            
            log.info("=== 数据导入完成 ===");
            log.info("所有数据已成功导入并翻译完成");
            
        } catch (Exception e) {
            log.error("数据导入过程中发生错误", e);
            throw e;
        }
        
        // 导入完成后退出应用
        log.info("数据导入模式完成，应用即将退出");
        System.exit(0);
    }
} 