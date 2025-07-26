package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "player_contracts", indexes = {
    @Index(name = "idx_contracts_player_team", columnList = "player_id, team_id")
})
public class PlayerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "market_value_eur")
    private Long marketValueEur;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "is_on_loan")
    private Boolean isOnLoan = false;
} 