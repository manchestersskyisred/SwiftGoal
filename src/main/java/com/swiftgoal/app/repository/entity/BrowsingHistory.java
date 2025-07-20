package com.swiftgoal.app.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "browsing_history")
@Getter
@Setter
public class BrowsingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_article_id", nullable = false)
    private NewsArticle newsArticle;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    public BrowsingHistory() {
    }

    public BrowsingHistory(User user, NewsArticle newsArticle) {
        this.user = user;
        this.newsArticle = newsArticle;
        this.viewedAt = LocalDateTime.now();
    }
} 