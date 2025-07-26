package com.swiftgoal.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.swiftgoal.app.service.FootballDataApiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final FootballDataApiClient apiClient;

    public TestController(FootballDataApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/api-test")
    public String testApi() {
        try {
            // 测试获取英超联赛信息
            JsonNode leagueInfo = apiClient.getLeagueInfo(FootballDataApiClient.LeagueCodes.PREMIER_LEAGUE);
            return "API测试成功！英超联赛信息：" + leagueInfo.get("name").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "API测试失败：" + e.getMessage();
        }
    }

    @GetMapping("/teams-test")
    public String testTeams() {
        try {
            // 测试获取英超球队
            JsonNode teams = apiClient.getTeamsByLeague(FootballDataApiClient.LeagueCodes.PREMIER_LEAGUE, 2025);
            int teamCount = teams.get("teams").size();
            return "成功获取英超球队，共" + teamCount + "支球队";
        } catch (Exception e) {
            e.printStackTrace();
            return "获取球队失败：" + e.getMessage();
        }
    }
} 