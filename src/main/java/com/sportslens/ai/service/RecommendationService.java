package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    public Page<NewsArticle> getRecommendationsForUserPaginated(User user, Pageable pageable) {
        if (user.getBrowsingHistory().isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> userKeywords = user.getBrowsingHistory().stream()
                .map(history -> history.getNewsArticle().getKeywordsAi())
                .filter(keywords -> keywords != null && !keywords.isEmpty())
                .flatMap(keywords -> Arrays.stream(keywords.split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (userKeywords.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> readArticleIds = user.getBrowsingHistory().stream()
                .map(history -> history.getNewsArticle().getId())
                .collect(Collectors.toList());
        
        // In-memory filtering and sorting - potential performance bottleneck
        List<NewsArticle> recommended = newsArticleRepository.findAll().stream()
                .filter(article -> !readArticleIds.contains(article.getId()))
                .filter(article -> article.getKeywordsAi() != null && !article.getKeywordsAi().isEmpty() && article.getTranslationStatus() == 1)
                .sorted((a1, a2) -> {
                    long a1Matches = countKeywordMatches(a1.getKeywordsAi(), userKeywords);
                    long a2Matches = countKeywordMatches(a2.getKeywordsAi(), userKeywords);
                    return Long.compare(a2Matches, a1Matches);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), recommended.size());

        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, recommended.size());
        }

        return new PageImpl<>(recommended.subList(start, end), pageable, recommended.size());
    }
    
    public List<NewsArticle> getRecommendationsForUser(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return getRecommendationsForUserPaginated(user, pageable).getContent();
    }

    private long countKeywordMatches(String articleKeywords, List<String> userKeywords) {
        if (articleKeywords == null || articleKeywords.isEmpty()) {
            return 0;
        }
        List<String> keywords = Arrays.asList(articleKeywords.split(","));
        return keywords.stream().map(String::trim).filter(userKeywords::contains).count();
    }
} 