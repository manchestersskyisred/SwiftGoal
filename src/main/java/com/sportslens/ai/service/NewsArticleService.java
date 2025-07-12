package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsArticleService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    public List<NewsArticle> findAll() {
        return newsArticleRepository.findAll(Sort.by(Sort.Direction.DESC, "publishDate"));
    }

    public List<NewsArticle> searchArticles(String query) {
        return newsArticleRepository.searchByTitleOrSummary(query);
    }

    public List<String> findDistinctCategories() {
        return newsArticleRepository.findDistinctCategoryAi();
    }

    public List<NewsArticle> findByCategory(String category) {
        return newsArticleRepository.findByCategoryAi(category);
    }
}
