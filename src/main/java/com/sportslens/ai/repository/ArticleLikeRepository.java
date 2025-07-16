package com.sportslens.ai.repository;

import com.sportslens.ai.domain.ArticleLike;
import com.sportslens.ai.domain.NewsArticle;
import com.sportslens.ai.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    
    /**
     * 查找用户对特定文章的点赞记录
     */
    Optional<ArticleLike> findByUserAndNewsArticle(User user, NewsArticle newsArticle);
    
    /**
     * 检查用户是否已经点赞过该文章
     */
    boolean existsByUserAndNewsArticle(User user, NewsArticle newsArticle);
    
    /**
     * 统计文章的点赞数量
     */
    long countByNewsArticle(NewsArticle newsArticle);
    
    /**
     * 删除用户对文章的点赞
     */
    void deleteByUserAndNewsArticle(User user, NewsArticle newsArticle);
    
    /**
     * 根据用户ID和文章ID查找点赞记录
     */
    @Query("SELECT al FROM ArticleLike al WHERE al.user.id = :userId AND al.newsArticle.id = :articleId")
    Optional<ArticleLike> findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    /**
     * 获取用户点赞的所有文章
     */
    List<ArticleLike> findByUserOrderByLikedAtDesc(User user);
    
    /**
     * 获取文章的所有点赞记录
     */
    List<ArticleLike> findByNewsArticleOrderByLikedAtDesc(NewsArticle newsArticle);
}