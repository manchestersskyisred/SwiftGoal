package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.repository.NewsArticleRepository;
import com.swiftgoal.app.repository.BrowsingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    public List<NewsArticle> getRecommendationsForUser(User user, int limit) {
        logger.info("Generating recommendations for user: {}", user.getUsername());

        List<String> userCategories = browsingHistoryRepository.findByUserOrderByViewedAtDesc(user).stream()
                .map(history -> history.getNewsArticle().getCategoryAi())
                .distinct()
                .collect(Collectors.toList());
        
        if (userCategories.isEmpty()) {
            logger.info("User {} has no browsing history. Returning latest articles.", user.getUsername());
            return newsArticleRepository.findNonUserGeneratedArticles().stream().limit(limit).collect(Collectors.toList());
        }

        logger.info("User {} has interest in categories: {}", user.getUsername(), userCategories);

        List<NewsArticle> recommendedArticles = userCategories.stream()
                .flatMap(category -> newsArticleRepository.findByCategory(category).stream())
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
        
        logger.info("Found {} recommended articles for user {}", recommendedArticles.size(), user.getUsername());

        return recommendedArticles;
    }

    public Page<NewsArticle> getRecommendationsForUserPaginated(User user, Pageable pageable) {
        logger.info("Generating paginated recommendations for user: {}", user.getUsername());

        List<String> userCategories = browsingHistoryRepository.findByUserOrderByViewedAtDesc(user).stream()
                .map(history -> history.getNewsArticle().getCategoryAi())
                .distinct()
                .collect(Collectors.toList());

        if (userCategories.isEmpty()) {
            logger.info("User {} has no browsing history. Returning latest articles.", user.getUsername());
            return newsArticleRepository.findAll(pageable);
        }

        logger.info("User {} has interest in categories: {}", user.getUsername(), userCategories);

        List<NewsArticle> recommendedArticles = userCategories.stream()
                .flatMap(category -> newsArticleRepository.findByCategory(category).stream())
                .distinct()
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), recommendedArticles.size());
        
        List<NewsArticle> pageContent = recommendedArticles.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, recommendedArticles.size());
    }

    public List<NewsArticle> getLatestArticles(int limit) {
        return newsArticleRepository.findNonUserGeneratedArticles().stream().limit(limit).collect(Collectors.toList());
    }

    public List<String> getAvailableCategories() {
        return newsArticleRepository.findDistinctCategories();
    }

    public List<NewsArticle> getArticlesByCategory(String category, int limit) {
        return newsArticleRepository.findByCategory(category).stream().limit(limit).collect(Collectors.toList());
    }
} 