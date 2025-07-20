package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NewsArticleService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    @Transactional(readOnly = true)
    public List<NewsArticle> findAllArticles() {
        return newsArticleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<NewsArticle> findArticleById(Long id) {
        return newsArticleRepository.findById(id);
    }
    
    @Transactional
    public NewsArticle saveArticle(NewsArticle article) {
        return newsArticleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        newsArticleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> findByUserId(Long userId) {
        return newsArticleRepository.findByUserIdOrderByPublishDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<NewsArticle> findByIdAndUserId(Long id, Long userId) {
        return newsArticleRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public Page<NewsArticle> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<NewsArticle> articlePage = newsArticleRepository.findAll(pageable);
        return articlePage != null ? articlePage : Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public List<NewsArticle> searchArticles(String query) {
        return newsArticleRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public Page<NewsArticle> findByCategoryPaginated(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<NewsArticle> articlePage = newsArticleRepository.findByCategoryAi(category, pageable);
        return articlePage != null ? articlePage : Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<NewsArticle> findById(Long id) {
        return newsArticleRepository.findById(id);
    }
}
