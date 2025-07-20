package com.swiftgoal.app.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "player_match_stats")
public class PlayerMatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private LocalDate matchDate;

    @Column(nullable = false)
    private String competitionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private Integer minutesPlayed;
    private Integer goals = 0;
    private Integer assists = 0;
    private Integer shots;
    private Integer shotsOnTarget;
    private Integer keyPasses;
    private Float playerRating;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private String heatmapData;
} 