package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.BrowsingHistoryRepository;
import com.swiftgoal.app.repository.entity.BrowsingHistory;
import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrowsingHistoryService {

    private final BrowsingHistoryRepository browsingHistoryRepository;

    public BrowsingHistoryService(BrowsingHistoryRepository browsingHistoryRepository) {
        this.browsingHistoryRepository = browsingHistoryRepository;
    }

    @Transactional
    public void recordHistory(User user, NewsArticle article) {
        // Create and save a new browsing history record.
        // A potential improvement for the future would be to prevent creating duplicate
        // records if the user views the same article multiple times in a short period.
        BrowsingHistory history = new BrowsingHistory(user, article);
        browsingHistoryRepository.save(history);
    }
} 