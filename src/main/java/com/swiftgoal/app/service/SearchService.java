package com.swiftgoal.app.service;

import com.swiftgoal.app.dto.SearchResultDto;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.Team;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.football.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public SearchService(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public List<SearchResultDto> searchPlayersAndTeams(String query) {
        List<SearchResultDto> results = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            return results;
        }
        
        String trimmedQuery = query.trim();
        
        // 搜索球员
        List<Player> players = playerRepository.searchPlayersByAnyField(trimmedQuery);
        results.addAll(players.stream()
                .map(this::convertPlayerToSearchResult)
                .collect(Collectors.toList()));
        
        // 搜索球队
        List<Team> teams = teamRepository.searchTeamsByAnyField(trimmedQuery);
        results.addAll(teams.stream()
                .map(this::convertTeamToSearchResult)
                .collect(Collectors.toList()));
        
        return results;
    }

    public List<SearchResultDto> searchPlayers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String trimmedQuery = query.trim();
        List<Player> players = playerRepository.searchPlayersByAnyField(trimmedQuery);
        
        return players.stream()
                .map(this::convertPlayerToSearchResult)
                .collect(Collectors.toList());
    }

    public List<SearchResultDto> searchTeams(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String trimmedQuery = query.trim();
        List<Team> teams = teamRepository.searchTeamsByAnyField(trimmedQuery);
        
        return teams.stream()
                .map(this::convertTeamToSearchResult)
                .collect(Collectors.toList());
    }

    private SearchResultDto convertPlayerToSearchResult(Player player) {
        String displayName = player.getFullName();
        if (player.getKnownAs() != null && !player.getKnownAs().isEmpty()) {
            displayName = player.getKnownAs();
        }
        
        String description = String.format("%s • %s • %s", 
                player.getPosition() != null ? player.getPosition() : "未知位置",
                player.getNationality() != null ? player.getNationality() : "未知国籍",
                player.getDateOfBirth() != null ? player.getDateOfBirth().getYear() + "年出生" : "未知年龄");
        
        return new SearchResultDto(
                "player",
                player.getId(),
                player.getFullName(),
                displayName,
                description,
                player.getPhotoUrl(),
                "/players/" + player.getId()
        );
    }

    private SearchResultDto convertTeamToSearchResult(Team team) {
        String description = String.format("%s • %s", 
                team.getCountry() != null ? team.getCountry() : "未知国家",
                team.getStadiumName() != null ? team.getStadiumName() : "未知主场");
        
        return new SearchResultDto(
                "team",
                team.getId(),
                team.getName(),
                team.getName(),
                description,
                team.getLogoUrl(),
                "/teams/" + team.getId()
        );
    }
} 