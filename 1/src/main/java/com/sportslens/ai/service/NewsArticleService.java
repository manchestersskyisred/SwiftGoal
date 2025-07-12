package com.sportslens.ai.service;

import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<NewsArticle> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        return newsArticleRepository.findAllByOrderByPublishDateDesc(pageable);
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
