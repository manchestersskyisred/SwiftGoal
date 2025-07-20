package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.PlayerContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerContractRepository extends JpaRepository<PlayerContract, Long> {
} 