package com.sportslens.ai.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "article_id"})
})
public class ArticleLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle newsArticle;
    
    @Column(nullable = false)
    private LocalDateTime likedAt;
    
    // 构造函数
    public ArticleLike() {}
    
    public ArticleLike(User user, NewsArticle newsArticle) {
        this.user = user;
        this.newsArticle = newsArticle;
        this.likedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public NewsArticle getNewsArticle() {
        return newsArticle;
    }
    
    public void setNewsArticle(NewsArticle newsArticle) {
        this.newsArticle = newsArticle;
    }
    
    public LocalDateTime getLikedAt() {
        return likedAt;
    }
    
    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }
}