package com.sportslens.ai.service;

import com.sportslens.ai.domain.Player;
import com.sportslens.ai.domain.Team;
import com.sportslens.ai.dto.PlayerDto;
import com.sportslens.ai.repository.PlayerRepository;
import com.sportslens.ai.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    public List<Player> findByTeamId(Long teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    public Player save(PlayerDto playerDto) {
        Team team = teamRepository.findById(playerDto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        Player player = new Player();
        player.setName(playerDto.getName());
        player.setPosition(playerDto.getPosition());
        player.setJerseyNumber(playerDto.getJerseyNumber());
        player.setTeam(team);
        
        return playerRepository.save(player);
    }

    public Player update(Long id, PlayerDto playerDto) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        
        Team team = teamRepository.findById(playerDto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        existingPlayer.setName(playerDto.getName());
        existingPlayer.setPosition(playerDto.getPosition());
        existingPlayer.setJerseyNumber(playerDto.getJerseyNumber());
        existingPlayer.setTeam(team);
        
        return playerRepository.save(existingPlayer);
    }

    public void deleteById(Long id) {
        playerRepository.deleteById(id);
    }
}