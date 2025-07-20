package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.service.NewsArticleService;
import com.swiftgoal.app.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsApiController {

    @Autowired
    private NewsArticleService newsArticleService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public List<NewsArticle> listNews() {
        return newsArticleService.findAllArticles();
    }

    @GetMapping("/latest")
    public List<NewsArticle> latestNews(@RequestParam(defaultValue = "10") int limit) {
        return recommendationService.getLatestArticles(limit);
    }

    @GetMapping("/categories")
    public List<String> listCategories() {
        return recommendationService.getAvailableCategories();
    }
} 