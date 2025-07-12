package com.sportslens.ai.repository;

import com.sportslens.ai.domain.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    Optional<NewsArticle> findByUrl(String url);

    @Query("SELECT a FROM NewsArticle a WHERE a.titleCn LIKE %:query% OR a.summaryAiCn LIKE %:query%")
    List<NewsArticle> searchByTitleOrSummary(@Param("query") String query);

    @Query("SELECT DISTINCT a.categoryAi FROM NewsArticle a WHERE a.categoryAi IS NOT NULL AND a.categoryAi <> ''")
    List<String> findDistinctCategoryAi();

    List<NewsArticle> findByCategoryAi(String categoryAi);
}