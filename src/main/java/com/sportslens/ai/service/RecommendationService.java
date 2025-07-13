package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class RecommendationService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    public List<NewsArticle> getRecommendationsForUser(User user, int limit) {
        // Get keywords from user's browsing history
        List<String> userKeywords = user.getBrowsingHistory().stream()
                .map(history -> history.getNewsArticle().getKeywordsAi())
                .filter(keywords -> keywords != null && !keywords.isEmpty())
                .flatMap(keywords -> Arrays.stream(keywords.split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (userKeywords.isEmpty()) {
            return List.of();
        }

        // Find articles with similar keywords that the user hasn't read
        List<Long> readArticleIds = user.getBrowsingHistory().stream()
                .map(history -> history.getNewsArticle().getId())
                .collect(Collectors.toList());

        // A non-existent ID is used to prevent errors with empty 'NOT IN' clauses.
        if (readArticleIds.isEmpty()) {
            readArticleIds.add(0L);
        }

        // Heuristic: consider only the 200 most recent unread articles for recommendation
        Pageable pageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "publishDate"));

        List<NewsArticle> candidateArticles = newsArticleRepository.findUnreadArticlesWithKeywords(readArticleIds, pageable);

        return candidateArticles.stream()
                .filter(article -> article.getKeywordsAi() != null)
                .sorted((a1, a2) -> {
                    long a1Matches = countKeywordMatches(a1.getKeywordsAi(), userKeywords);
                    long a2Matches = countKeywordMatches(a2.getKeywordsAi(), userKeywords);
                    return Long.compare(a2Matches, a1Matches);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private long countKeywordMatches(String articleKeywords, List<String> userKeywords) {
        List<String> keywords = Arrays.asList(articleKeywords.split(","));
        return keywords.stream().filter(userKeywords::contains).count();
    }
} 