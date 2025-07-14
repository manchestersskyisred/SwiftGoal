package com.sportslens.ai.repository;

import com.sportslens.ai.domain.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    Optional<NewsArticle> findByUrl(String url);
    
    List<NewsArticle> findByUserIdOrderByPublishDateDesc(Long userId);
    
    Optional<NewsArticle> findByIdAndUserId(Long id, Long userId);
    
    List<NewsArticle> findByUserGeneratedTrueOrderByPublishDateDesc();
    
    @Query("SELECT a FROM NewsArticle a WHERE a.userGenerated = false AND a.translationStatus = 1 ORDER BY a.publishDate DESC")
    List<NewsArticle> findPublishedArticles();

    @Query("SELECT a FROM NewsArticle a WHERE (a.titleCn LIKE %:query% OR a.summaryAiCn LIKE %:query% OR a.title LIKE %:query%) AND a.translationStatus = 1")
    List<NewsArticle> searchByTitleOrSummary(@Param("query") String query);

    @Query("SELECT DISTINCT a.categoryAi FROM NewsArticle a WHERE a.categoryAi IS NOT NULL AND a.categoryAi <> '' AND a.translationStatus = 1")
    List<String> findDistinctCategoryAi();

    @Query("SELECT a FROM NewsArticle a WHERE a.categoryAi = :categoryAi ORDER BY a.publishDate DESC")
    Page<NewsArticle> findAllByCategoryAi(@Param("categoryAi") String categoryAi, Pageable pageable);

    @Query("SELECT a FROM NewsArticle a WHERE a.categoryAi = :categoryAi AND a.translationStatus = 1")
    List<NewsArticle> findByCategoryAi(@Param("categoryAi") String categoryAi);

    @Query("SELECT a FROM NewsArticle a WHERE a.categoryAi = :categoryAi AND a.translationStatus = 1 ORDER BY a.publishDate DESC")
    Page<NewsArticle> findByCategoryAi(@Param("categoryAi") String categoryAi, Pageable pageable);

    @Query("SELECT a FROM NewsArticle a WHERE a.translationStatus = 1")
    Page<NewsArticle> findAllTranslated(Pageable pageable);
}