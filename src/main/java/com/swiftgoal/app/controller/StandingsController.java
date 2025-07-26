package com.swiftgoal.app.controller;

import com.swiftgoal.app.dto.StandingsRowDto;
import com.swiftgoal.app.repository.entity.football.League;
import com.swiftgoal.app.repository.football.LeagueRepository;
import com.swiftgoal.app.service.FootballDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/standings")
public class StandingsController {

    private final FootballDataService footballDataService;
    private final LeagueRepository leagueRepository;
    private static final int CURRENT_SEASON = 2023;

    public StandingsController(FootballDataService footballDataService, LeagueRepository leagueRepository) {
        this.footballDataService = footballDataService;
        this.leagueRepository = leagueRepository;
    }

    @GetMapping("/{leagueId}")
    public String getStandings(@PathVariable Long leagueId, Model model) {
        Optional<League> leagueOpt = leagueRepository.findById(leagueId);
        if (leagueOpt.isPresent()) {
            League league = leagueOpt.get();
            List<StandingsRowDto> standings = footballDataService.getLeagueStandings(league.getApiLeagueId(), CURRENT_SEASON);
            model.addAttribute("leagueName", league.getName());
            model.addAttribute("standings", standings);
        }
        return "standings";
    }
} 