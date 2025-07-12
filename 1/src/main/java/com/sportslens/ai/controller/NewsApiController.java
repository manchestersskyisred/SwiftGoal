package com.sportslens.ai.controller;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.service.NewsArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
public class NewsApiController {

    private final NewsArticleService newsArticleService;

    @Autowired
    public NewsApiController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public Page<NewsArticle> getNews(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        return newsArticleService.findPaginated(page, size);
    }
} 