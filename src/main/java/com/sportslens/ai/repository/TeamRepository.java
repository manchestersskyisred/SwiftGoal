package com.sportslens.ai.repository;

import com.sportslens.ai.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}