package com.swiftgoal.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FootballDataApiClient {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Value("${api.football-data.token}")
    private String apiToken;

    @Value("${api.football-data.base-url}")
    private String baseUrl;

    public FootballDataApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取联赛的球队列表
     */
    public JsonNode getTeamsByLeague(String leagueCode, int season) throws IOException {
        String url = String.format("%s/competitions/%s/teams?season=%d", baseUrl, leagueCode, season);
        return makeRequest(url);
    }

    /**
     * 获取球队的球员列表
     */
    public JsonNode getPlayersByTeam(int teamId) throws IOException {
        String url = String.format("%s/teams/%d", baseUrl, teamId);
        return makeRequest(url);
    }

    /**
     * 获取联赛信息
     */
    public JsonNode getLeagueInfo(String leagueCode) throws IOException {
        String url = String.format("%s/competitions/%s", baseUrl, leagueCode);
        return makeRequest(url);
    }

    /**
     * 获取联赛排名
     */
    public JsonNode getLeagueStandings(String leagueCode, int season) throws IOException {
        String url = String.format("%s/competitions/%s/standings?season=%d", baseUrl, leagueCode, season);
        return makeRequest(url);
    }

    /**
     * 获取球员详细信息
     */
    public JsonNode getPlayerInfo(int playerId) throws IOException {
        String url = String.format("%s/persons/%d", baseUrl, playerId);
        return makeRequest(url);
    }

    /**
     * 通用请求方法
     */
    private JsonNode makeRequest(String url) throws IOException {
        log.info("Making request to: {}", url);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Auth-Token", apiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Response received: {}", responseBody);
            
            return objectMapper.readTree(responseBody);
        } catch (Exception e) {
            log.error("Error making request to {}: {}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取联赛代码映射
     */
    public static class LeagueCodes {
        public static final String PREMIER_LEAGUE = "PL";
        public static final String LA_LIGA = "PD";
        public static final String BUNDESLIGA = "BL1";
        public static final String SERIE_A = "SA";
        public static final String LIGUE_1 = "FL1";
        public static final String CHAMPIONS_LEAGUE = "CL";
        public static final String EUROPA_LEAGUE = "EL";
    }
} 