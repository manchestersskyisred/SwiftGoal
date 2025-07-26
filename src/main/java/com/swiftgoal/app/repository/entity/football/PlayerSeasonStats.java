package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "player_season_stats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "team_id", "league_id", "season"})
})
public class PlayerSeasonStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;
    
    @Column(nullable = false)
    private int season;
    
    private Integer minutesPlayed;
    private Integer appearances;
    private Integer lineups;
    private Integer goals;
    private Integer assists;
} 