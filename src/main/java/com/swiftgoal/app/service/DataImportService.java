package com.swiftgoal.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.swiftgoal.app.repository.entity.football.*;
import com.swiftgoal.app.repository.football.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DataImportService {

    private final FootballDataApiClient apiClient;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerContractRepository playerContractRepository;
    
    private static final Map<String, String> LEAGUE_CODES = new HashMap<>();
    private static final int CURRENT_SEASON = 2025; // 赛季设置为2025

    static {
        LEAGUE_CODES.put("Premier League", "PL");
        LEAGUE_CODES.put("La Liga", "PD");
        LEAGUE_CODES.put("Bundesliga", "BL1");
        LEAGUE_CODES.put("Serie A", "SA");
        LEAGUE_CODES.put("Ligue 1", "FL1");
    }

    public DataImportService(FootballDataApiClient apiClient, LeagueRepository leagueRepository, TeamRepository teamRepository, PlayerRepository playerRepository, PlayerContractRepository playerContractRepository) {
        this.apiClient = apiClient;
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.playerContractRepository = playerContractRepository;
    }

    public void importLeaguesTeamsAndPlayers() {
        log.info("Starting data import for season {} using Football-Data.org API...", CURRENT_SEASON);
        LEAGUE_CODES.forEach((leagueName, leagueCode) -> {
            try {
                importLeague(leagueName, leagueCode, CURRENT_SEASON);
                log.info("Successfully imported league: {}. Waiting before next call...", leagueName);
                TimeUnit.SECONDS.sleep(6); // Respect API rate limits (10 calls/minute on free plan)
            } catch (Exception e) {
                log.error("Failed to import data for league: {}. Moving to the next one.", leagueName, e);
            }
        });
        log.info("Data import finished.");
    }
    
    @Transactional
    private void importLeague(String leagueName, String leagueCode, int season) throws IOException {
        log.info("Processing league: {} with code: {}", leagueName, leagueCode);
        
        // 获取联赛信息
        JsonNode leagueInfo = apiClient.getLeagueInfo(leagueCode);
        League league = leagueRepository.findByNameEn(leagueName).orElseGet(() -> {
            League newLeague = new League();
            newLeague.setNameEn(leagueName);
            return newLeague;
        });
        
        // 更新联赛信息
        if (leagueInfo.has("area")) {
            league.setCountryEn(leagueInfo.get("area").get("name").asText());
        }
        leagueRepository.save(league);

        // 获取联赛的球队
        JsonNode teamsResponse = apiClient.getTeamsByLeague(leagueCode, season);
        JsonNode teamsArray = teamsResponse.get("teams");

        if (teamsArray != null && teamsArray.isArray() && teamsArray.size() > 0) {
            for (JsonNode teamNode : teamsArray) {
                importTeam(teamNode, league, season);
                try {
                    TimeUnit.SECONDS.sleep(1); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted during team import", e);
                }
            }
        } else {
            log.warn("No teams found for league {} in season {}. API response might be empty.", leagueName, season);
        }
    }

    private void importTeam(JsonNode teamNode, League league, int season) throws IOException {
        int apiTeamId = teamNode.get("id").asInt();
        String teamName = teamNode.get("name").asText();

        log.info("Importing team: {}", teamName);

        Team team = teamRepository.findByApiTeamId(apiTeamId).orElseGet(() -> {
            return teamRepository.findByNameEn(teamName).orElseGet(()-> {
                Team newTeam = new Team();
                newTeam.setApiTeamId(apiTeamId);
                return newTeam;
            });
        });
        team.setApiTeamId(apiTeamId);
        
        team.setNameEn(teamName);
        team.setCountryEn(teamNode.get("area").get("name").asText());
        team.setLogoUrl(teamNode.get("crest").asText());
        if (teamNode.has("venue")) {
            team.setStadiumNameEn(teamNode.get("venue").asText());
        }
        team.setLeague(league);
        teamRepository.save(team);
        
        try {
            log.debug("Waiting before fetching players for team {} to respect API rate limit...", team.getNameEn());
            TimeUnit.SECONDS.sleep(6); // Respect API rate limits (10 calls/minute on free plan)
            importPlayersForTeam(team, season);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting to fetch players for team {}", team.getNameEn(), e);
        }
    }
    
    private void importPlayersForTeam(Team team, int season) throws IOException {
        JsonNode response = apiClient.getPlayersByTeam(team.getApiTeamId());
        JsonNode playersArray = response.get("squad");
        if (playersArray != null && playersArray.isArray()) {
            for(JsonNode playerNode : playersArray) {
                int apiPlayerId = playerNode.get("id").asInt();
                String fullName = playerNode.get("name").asText();
                log.info("Importing player: {}", fullName);

                Player player = playerRepository.findByApiPlayerId(apiPlayerId).orElseGet(() -> {
                    Player newPlayer = new Player();
                    newPlayer.setApiPlayerId(apiPlayerId);
                    return newPlayer;
                });
                player.setFullNameEn(fullName);
                
                // 处理姓名
                String[] nameParts = fullName.split(" ", 2);
                if (nameParts.length > 1) {
                    player.setFirstName(nameParts[0]);
                    player.setLastName(nameParts[1]);
                } else {
                    player.setFirstName(fullName);
                    player.setLastName("");
                }
                
                player.setNationalityEn(playerNode.get("nationality").asText());
                if (playerNode.has("photo")) {
                    player.setPhotoUrl(playerNode.get("photo").asText());
                }
                
                // 处理出生日期
                if (playerNode.has("dateOfBirth")) {
                    String birthDateStr = playerNode.get("dateOfBirth").asText();
                    if(birthDateStr != null && !birthDateStr.equals("null") && !birthDateStr.isEmpty()) {
                        try {
                            player.setDateOfBirth(LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE));
                        } catch (DateTimeParseException e) {
                            log.warn("Could not parse birth date '{}' for player {}", birthDateStr, fullName);
                            player.setDateOfBirth(null);
                        }
                    } else {
                        player.setDateOfBirth(null);
                    }
                } else {
                    player.setDateOfBirth(null);
                }
                
                // 处理位置
                if (playerNode.has("position")) {
                    String position = playerNode.get("position").asText();
                    player.setPositionEn(position);
                } else {
                    player.setPositionEn("N/A");
                }
                
                playerRepository.save(player);

                // 创建或更新合同
                PlayerContract contract = playerContractRepository.findByPlayerAndTeam(player, team).orElseGet(PlayerContract::new);
                contract.setPlayer(player);
                contract.setTeam(team);
                
                // 处理球衣号码
                if (playerNode.has("shirtNumber")) {
                    JsonNode jerseyNode = playerNode.get("shirtNumber");
                    if (jerseyNode != null && !jerseyNode.isNull()) {
                        contract.setJerseyNumber(jerseyNode.asInt());
                    }
                }
                
                playerContractRepository.save(contract);
            }
        } else {
            log.warn("No players found for team {} in season {}", team.getNameEn(), season);
        }
    }
} 