package com.swiftgoal.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.swiftgoal.app.dto.StandingsRowDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FootballDataService {

    private final FootballApiClient footballApiClient;

    public FootballDataService(FootballApiClient footballApiClient) {
        this.footballApiClient = footballApiClient;
    }

    public List<StandingsRowDto> getLeagueStandings(int leagueId, int season) {
        try {
            JsonNode response = footballApiClient.getStandingsByLeague(leagueId, season);
            JsonNode standingsArray = response.get("response").get(0).get("league").get("standings").get(0);
            
            if (standingsArray != null && standingsArray.isArray()) {
                List<StandingsRowDto> standings = new ArrayList<>();
                for (JsonNode row : standingsArray) {
                    standings.add(StandingsRowDto.builder()
                            .rank(row.get("rank").asInt())
                            .teamName(row.get("team").get("name").asText())
                            .teamLogoUrl(row.get("team").get("logo").asText())
                            .points(row.get("points").asInt())
                            .played(row.get("all").get("played").asInt())
                            .win(row.get("all").get("win").asInt())
                            .draw(row.get("all").get("draw").asInt())
                            .lose(row.get("all").get("lose").asInt())
                            .goalsFor(row.get("all").get("goals").get("for").asInt())
                            .goalsAgainst(row.get("all").get("goals").get("against").asInt())
                            .goalsDiff(row.get("goalsDiff").asInt())
                            .form(row.get("form").asText())
                            .build());
                }
                return standings;
            }
        } catch (IOException | NullPointerException e) {
            log.error("Error fetching or parsing standings for leagueId: {}", leagueId, e);
        }
        return Collections.emptyList();
    }
} 