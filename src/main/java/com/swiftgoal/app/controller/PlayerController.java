package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerMatchStats;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.service.PlayerDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final PlayerDataService playerDataService;

    public PlayerController(PlayerRepository playerRepository, PlayerDataService playerDataService) {
        this.playerRepository = playerRepository;
        this.playerDataService = playerDataService;
    }

    @GetMapping("/{id}")
    public String getPlayerDetails(@PathVariable Long id, Model model) {
        Optional<Player> playerOpt = playerRepository.findById(id);
        if (playerOpt.isPresent()) {
            model.addAttribute("player", playerOpt.get());
            // We can add more details like season stats later
        } else {
            return "redirect:/"; // Or a proper 404 page
        }
        return "player_detail";
    }

    @GetMapping("/{playerId}/seasons/{season}/matches")
    @ResponseBody
    public ResponseEntity<List<PlayerMatchStats>> getPlayerMatchesForSeason(
            @PathVariable Long playerId,
            @PathVariable int season) {
        try {
            List<PlayerMatchStats> stats = playerDataService.fetchAndSavePlayerMatchStats(playerId, season);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }
} 