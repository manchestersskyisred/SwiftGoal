package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    Optional<NewsArticle> findByUrl(String url);

    List<NewsArticle> findByUserIdOrderByPublishDateDesc(Long userId);

    Optional<NewsArticle> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT na FROM NewsArticle na WHERE na.userGenerated = false ORDER BY na.publishDate DESC")
    List<NewsArticle> findNonUserGeneratedArticles();

    @Query("SELECT na FROM NewsArticle na WHERE na.categoryAi = :category AND na.userGenerated = false ORDER BY na.publishDate DESC")
    List<NewsArticle> findByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT n.categoryAi FROM NewsArticle n WHERE n.categoryAi IS NOT NULL AND n.categoryAi <> ''")
    List<String> findDistinctCategories();

    List<NewsArticle> findByTitleContainingIgnoreCase(String title);

    Page<NewsArticle> findByCategoryAi(String categoryAi, Pageable pageable);

    Page<NewsArticle> findByTitleCnNotIn(List<String> titles, Pageable pageable);
}