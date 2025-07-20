package com.swiftgoal.app.repository;

import com.swiftgoal.app.repository.entity.ArticleComment;
import com.swiftgoal.app.repository.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    List<ArticleComment> findByNewsArticleOrderByCreatedAtDesc(NewsArticle newsArticle);
} 