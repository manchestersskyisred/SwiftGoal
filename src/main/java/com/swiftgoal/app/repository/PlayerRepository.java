package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.Player;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByfullNameContainingIgnoreCase(String name);
} 