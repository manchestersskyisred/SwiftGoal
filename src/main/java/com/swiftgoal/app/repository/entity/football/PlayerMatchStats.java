package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "player_match_stats", indexes = {
    @Index(name = "idx_match_stats_player_season", columnList = "player_id, match_date DESC")
})
public class PlayerMatchStats {
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
    @JoinColumn(name = "opponent_id", nullable = false)
    private Team opponent;

    @Column(name = "fixture_api_id", unique = true)
    private Long fixtureApiId;
    
    @Column(nullable = false)
    private String competitionName;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    private String venueName;
    private Boolean isHomeMatch;
    private Integer minutesPlayed;
    private Float playerRating;
    private Boolean isStarter;
    private Boolean isSubstitute;
    private Integer goals = 0;
    private Integer assists = 0;
    private Integer shotsTotal;
    private Integer shotsOnGoal;
    private Integer passesTotal;
    private Integer passesKey;
    private Integer dribblesAttempts;
    private Integer dribblesSuccess;
    private Integer tacklesTotal;
    private Integer interceptions;
    private Integer duelsTotal;
    private Integer duelsWon;
    private Boolean yellowCard = false;
    private Boolean redCard = false;
} 