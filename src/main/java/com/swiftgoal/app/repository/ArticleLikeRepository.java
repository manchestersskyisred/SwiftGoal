package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.ArticleLike;
import com.swiftgoal.app.repository.entity.NewsArticle;
import com.swiftgoal.app.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByUserAndNewsArticle(User user, NewsArticle newsArticle);
    long countByNewsArticle(NewsArticle newsArticle);
} 