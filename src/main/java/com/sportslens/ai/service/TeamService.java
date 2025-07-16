package com.sportslens.ai.service;

import com.sportslens.ai.domain.Team;
import com.sportslens.ai.dto.TeamDto;
import com.sportslens.ai.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    public Team save(TeamDto teamDto) {
        Team team = new Team();
        team.setName(teamDto.getName());
        team.setLeague(teamDto.getLeague());
        team.setCity(teamDto.getCity());
        
        return teamRepository.save(team);
    }

    public Team update(Long id, TeamDto teamDto) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        existingTeam.setName(teamDto.getName());
        existingTeam.setLeague(teamDto.getLeague());
        existingTeam.setCity(teamDto.getCity());
        
        return teamRepository.save(existingTeam);
    }

    public void deleteById(Long id) {
        teamRepository.deleteById(id);
    }
}