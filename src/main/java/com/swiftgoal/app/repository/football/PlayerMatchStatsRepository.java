package com.swiftgoal.app.repository.football;

import com.swiftgoal.app.repository.entity.football.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {

    boolean existsByFixtureApiId(Long fixtureApiId);
    
    // This could be used for the caching logic you designed
    // List<PlayerMatchStats> findByPlayerIdAndMatchDateBetween(Long playerId, LocalDate start, LocalDate end);
} 