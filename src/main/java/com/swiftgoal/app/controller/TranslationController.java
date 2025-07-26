package com.swiftgoal.app.controller;

import com.swiftgoal.app.service.ChineseNameTranslationService;
import com.swiftgoal.app.service.MarketValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.swiftgoal.app.dto.TranslationResultDto;

@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
@Slf4j
public class TranslationController {

    private final ChineseNameTranslationService translationService;
    private final MarketValueService marketValueService;

    /**
     * Translate all names using pre-defined maps.
     */
    @PostMapping("/scrape-players")
    public ResponseEntity<String> scrapeAllPlayerNames() {
        // Run in a new thread to avoid blocking the API response
        new Thread(translationService::translateAllPlayersWithScraper).start();
        return ResponseEntity.ok("批量爬虫翻译任务已在后台启动。");
    }

    /**
     * Scrape and translate a single player's name by their ID.
     *
     * @param playerId the player's database ID
     * @return a map indicating success or failure
     */
    @PostMapping("/scrape-player/{playerId}")
    public ResponseEntity<Map<String, Object>> scrapeSinglePlayer(@PathVariable Long playerId) {
        boolean success = translationService.translateSinglePlayerWithScraper(playerId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Scraping and translation successful." : "Scraping and translation failed or was not necessary.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/market-value/scrape-all-players")
    public ResponseEntity<String> scrapeAllPlayerMarketValues() {
        new Thread(marketValueService::scrapeMarketValueForAllPlayers).start();
        return ResponseEntity.ok("Market value scraping process started in the background.");
    }

    /**
     * 执行所有中文名翻译
     */
    @PostMapping("/translate-all")
    public ResponseEntity<Map<String, Object>> translateAllNames() {
        try {
            translationService.translateAllNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "所有中文名翻译完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("翻译过程中发生错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "翻译失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 翻译球员中文名
     */
    @PostMapping("/translate-players")
    public ResponseEntity<Map<String, Object>> translatePlayerNames() {
        try {
            translationService.translatePlayerNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "球员中文名翻译完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("球员翻译过程中发生错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "球员翻译失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 翻译球队中文名
     */
    @PostMapping("/translate-teams")
    public ResponseEntity<Map<String, Object>> translateTeamNames() {
        try {
            translationService.translateTeamNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "球队中文名翻译完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("球队翻译过程中发生错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "球队翻译失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 翻译联赛中文名
     */
    @PostMapping("/translate-leagues")
    public ResponseEntity<Map<String, Object>> translateLeagueNames() {
        try {
            translationService.translateLeagueNames();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "联赛中文名翻译完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("联赛翻译过程中发生错误", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "联赛翻译失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get the Chinese name for a player by their English name.
     *
     * @param englishName the player's English name
     * @return a map containing the names
     */
    @GetMapping("/player/{englishName}")
    public ResponseEntity<Map<String, Object>> getPlayerChineseName(@PathVariable String englishName) {
        String chineseName = translationService.getPlayerChineseName(englishName);
        Map<String, Object> response = new HashMap<>();
        response.put("englishName", englishName);
        response.put("chineseName", chineseName);
        response.put("found", chineseName != null);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取球队中文名
     */
    @GetMapping("/team/{englishName}")
    public ResponseEntity<Map<String, Object>> getTeamChineseName(@PathVariable String englishName) {
        String chineseName = translationService.getTeamChineseName(englishName);
        Map<String, Object> response = new HashMap<>();
        response.put("englishName", englishName);
        response.put("chineseName", chineseName);
        response.put("found", chineseName != null);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取联赛中文名
     */
    @GetMapping("/league/{englishName}")
    public ResponseEntity<Map<String, Object>> getLeagueChineseName(@PathVariable String englishName) {
        String chineseName = translationService.getLeagueChineseName(englishName);
        Map<String, Object> response = new HashMap<>();
        response.put("englishName", englishName);
        response.put("chineseName", chineseName);
        response.put("found", chineseName != null);
        return ResponseEntity.ok(response);
    }
} 