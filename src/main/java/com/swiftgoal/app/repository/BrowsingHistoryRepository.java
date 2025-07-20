package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.BrowsingHistory;
import com.swiftgoal.app.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {
    List<BrowsingHistory> findByUserOrderByViewedAtDesc(User user);
}