package com.swiftgoal.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerMatchStats;
import com.swiftgoal.app.repository.entity.football.Team;
import com.swiftgoal.app.repository.football.PlayerMatchStatsRepository;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.football.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PlayerDataService {

    private final FootballApiClient apiClient;
    private final PlayerMatchStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;


    public PlayerDataService(FootballApiClient apiClient, PlayerMatchStatsRepository statsRepository, PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.apiClient = apiClient;
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public List<PlayerMatchStats> fetchAndSavePlayerMatchStats(Long playerId, int season) {
        // Since we don't have a direct season link on PlayerMatchStats, we cannot implement the cache check as designed.
        // We will fetch from API and rely on the unique constraint on fixture_api_id to prevent duplicates.

        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.warn("Player with id {} not found.", playerId);
            return Collections.emptyList();
        }
        Player player = playerOpt.get();

        try {
            JsonNode fixturesResponse = apiClient.getFixturesByPlayer(player.getApiPlayerId(), season);
            if (fixturesResponse == null || !fixturesResponse.has("response")) {
                return Collections.emptyList();
            }

            List<PlayerMatchStats> newStats = new ArrayList<>();
            for (JsonNode fixtureNode : fixturesResponse.get("response")) {
                int fixtureId = fixtureNode.get("fixture").get("id").asInt();
                if (!statsRepository.existsByFixtureApiId((long) fixtureId)) {
                    log.info("Fetching stats for player {} in fixture {}", player.getFullName(), fixtureId);
                     // Respect API rate limits
                    TimeUnit.SECONDS.sleep(6);
                    JsonNode statsResponse = apiClient.getPlayerStatsForFixture(fixtureId);
                    
                    if (statsResponse != null && statsResponse.has("response")) {
                        // The response contains a list of players for the fixture. We need to find our player.
                        for(JsonNode playerFixtureNode : statsResponse.get("response")) {
                            for(JsonNode playerStatsNode : playerFixtureNode.get("players")){
                                if(playerStatsNode.get("player").get("id").asInt() == player.getApiPlayerId()){
                                    newStats.add(parseAndSaveMatchStats(playerStatsNode, player, fixtureNode));
                                }
                            }
                        }
                    }
                }
            }
            return newStats;

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching match stats for player id {} and season {}", playerId, season, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    private PlayerMatchStats parseAndSaveMatchStats(JsonNode statsNode, Player player, JsonNode fixtureNode) {
        PlayerMatchStats stats = new PlayerMatchStats();
        stats.setPlayer(player);
        stats.setFixtureApiId(fixtureNode.get("fixture").get("id").longValue());
        stats.setMatchDate(LocalDate.parse(fixtureNode.get("fixture").get("date").asText().substring(0, 10)));
        stats.setCompetitionName(fixtureNode.get("league").get("name").asText());
        
        Team homeTeam = teamRepository.findByApiTeamId(fixtureNode.get("teams").get("home").get("id").asInt()).orElse(null);
        Team awayTeam = teamRepository.findByApiTeamId(fixtureNode.get("teams").get("away").get("id").asInt()).orElse(null);
        
        if (homeTeam == null || awayTeam == null) return null; // Or handle more gracefully

        boolean isHome = statsNode.get("team").get("id").asInt() == homeTeam.getApiTeamId();
        stats.setTeam(isHome ? homeTeam : awayTeam);
        stats.setOpponent(isHome ? awayTeam : homeTeam);
        stats.setIsHomeMatch(isHome);

        JsonNode statsDetails = statsNode.get("statistics").get(0);
        stats.setMinutesPlayed(statsDetails.get("games").get("minutes").asInt(0));
        stats.setPlayerRating(statsDetails.get("games").get("rating") != null ? (float) statsDetails.get("games").get("rating").asDouble(0) : null);
        stats.setIsStarter(statsDetails.get("games").get("substitute").asBoolean(false));

        stats.setGoals(statsDetails.get("goals").get("total").asInt(0));
        stats.setAssists(statsDetails.get("goals").get("assists").asInt(0));
        
        stats.setShotsTotal(statsDetails.get("shots").get("total").asInt(0));
        stats.setShotsOnGoal(statsDetails.get("shots").get("on").asInt(0));
        
        stats.setPassesTotal(statsDetails.get("passes").get("total").asInt(0));
        stats.setPassesKey(statsDetails.get("passes").get("key").asInt(0));

        stats.setDribblesAttempts(statsDetails.get("dribbles").get("attempts").asInt(0));
        stats.setDribblesSuccess(statsDetails.get("dribbles").get("success").asInt(0));

        stats.setTacklesTotal(statsDetails.get("tackles").get("total").asInt(0));
        stats.setInterceptions(statsDetails.get("tackles").get("interceptions").asInt(0));

        stats.setDuelsTotal(statsDetails.get("duels").get("total").asInt(0));
        stats.setDuelsWon(statsDetails.get("duels").get("won").asInt(0));

        stats.setYellowCard(statsDetails.get("cards").get("yellow").asInt(0) > 0);
        stats.setRedCard(statsDetails.get("cards").get("red").asInt(0) > 0);

        return statsRepository.save(stats);
    }
} 