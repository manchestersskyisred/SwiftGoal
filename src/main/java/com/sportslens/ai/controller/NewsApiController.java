package com.sportslens.ai.controller;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.service.NewsArticleService;
import com.sportslens.ai.domain.User;
import com.sportslens.ai.repository.UserRepository;
import com.sportslens.ai.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
public class NewsApiController {

    @Autowired
    private NewsArticleService newsArticleService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public Page<NewsArticle> getNews(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "100") int size) {
        return newsArticleService.findPaginated(page, size);
    }

    @GetMapping("/recommendations")
    public Page<NewsArticle> getRecommendedArticles(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        if (userDetails == null) {
            return Page.empty();
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    Pageable pageable = PageRequest.of(page, size);
                    return recommendationService.getRecommendationsForUserPaginated(user, pageable);
                })
                .orElse(Page.empty());
    }

    @GetMapping("/category/{category}")
    public Page<NewsArticle> getCategoryArticles(@PathVariable String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        return newsArticleService.findByCategoryPaginated(category, page, size);
    }
} 