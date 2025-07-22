package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.Player;
import com.swiftgoal.app.repository.entity.Team;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Player> findByNameContainingIgnoreCase(String name);
} 