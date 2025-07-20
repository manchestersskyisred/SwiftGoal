package com.swiftgoal.app.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "player_season_stats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "season", "competition_name"})
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

    @Column(nullable = false, length = 50)
    private String season;

    @Column(name = "competition_name", nullable = false)
    private String competitionName;

    private Integer matchesPlayed = 0;
    private Integer matchesStarted = 0;
    private Integer minutesPlayed = 0;
    private Integer goals = 0;
    private Integer assists = 0;
    private Integer yellowCards = 0;
    private Integer redCards = 0;
    private Float expectedGoals;
    private Float expectedAssists;
} 