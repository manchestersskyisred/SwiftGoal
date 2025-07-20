package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {
} 