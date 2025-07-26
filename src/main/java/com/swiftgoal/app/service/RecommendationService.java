package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import com.swiftgoal.app.dto.NewsArticleDto;
import com.swiftgoal.app.repository.BrowsingHistoryRepository;
import com.swiftgoal.app.repository.NewsArticleRepository;
import com.swiftgoal.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.swiftgoal.app.repository.entity.BrowsingHistory;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepository userRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    private final NewsArticleService newsArticleService;

    public RecommendationService(UserRepository userRepository, NewsArticleRepository newsArticleRepository, BrowsingHistoryRepository browsingHistoryRepository, NewsArticleService newsArticleService) {
        this.userRepository = userRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.newsArticleService = newsArticleService;
    }

    @Transactional(readOnly = true)
    public List<NewsArticleDto> getLatestArticles(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("publishDate").descending());
        return newsArticleRepository.findAll(pageRequest).stream()
                .map(NewsArticleDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableCategories() {
        return newsArticleRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> getDefaultRecommendations(Set<Long> seenArticleIds, int limit) {
        // Fallback: return latest articles excluding seen ones
        PageRequest pageRequest = PageRequest.of(0, limit + seenArticleIds.size(), Sort.by("publishDate").descending());

        return newsArticleRepository.findAll(pageRequest).stream()
                .filter(article -> !seenArticleIds.contains(article.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<NewsArticle> getRecommendationsForUserPaginated(User user, Pageable pageable) {
        logger.info("Generating paginated recommendations for user: {}", user.getUsername());

        List<Long> userHistoryArticleIds = browsingHistoryRepository.findByUserOrderByViewedAtDesc(user).stream()
                .map(history -> history.getNewsArticle().getId())
                .collect(Collectors.toList());

        if (userHistoryArticleIds.isEmpty()) {
            logger.info("User {} has no browsing history. Returning latest articles.", user.getUsername());
            return newsArticleRepository.findAll(pageable);
        }

        Map<String, Long> categoryFrequency = userHistoryArticleIds.stream()
                .map(newsArticleService::findById) // Use the service to safely find articles by ID
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NewsArticle::getCategoryAi)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (categoryFrequency.isEmpty()) {
            logger.info("User {} has no valid categories in history. Returning latest articles.", user.getUsername());
            return newsArticleRepository.findAll(pageable);
        }

        List<String> sortedCategories = categoryFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info("User {} has interest in categories: {}", user.getUsername(), sortedCategories);

        List<NewsArticle> recommendedArticles = sortedCategories.stream()
                .flatMap(category -> newsArticleRepository.findByCategoryAi(category, PageRequest.of(0, 20)).stream())
                .distinct()
                .filter(article -> !userHistoryArticleIds.contains(article.getId()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), recommendedArticles.size());

        return new PageImpl<>(recommendedArticles.subList(start, end), pageable, recommendedArticles.size());
    }

    public List<NewsArticle> getArticlesByCategory(String category, int limit) {
        return newsArticleRepository.findByCategory(category).stream().limit(limit).collect(Collectors.toList());
    }
} 