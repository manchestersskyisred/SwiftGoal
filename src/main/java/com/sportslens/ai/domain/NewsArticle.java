package com.sportslens.ai.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_articles")
@Getter
@Setter
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, length = 512)
    private String url;

    private String source;
    private LocalDateTime publishDate;

    @Column(columnDefinition = "TEXT")
    private String rawContent;

    @Column(columnDefinition = "TEXT")
    private String rawHtmlContent; // To store original article HTML

    @Column(columnDefinition = "TEXT")
    private String translatedContent; // To store translated article content

    @Column(columnDefinition = "TEXT")
    private String titleCn;

    @Column(columnDefinition = "TEXT")
    private String summaryAi;

    @Column(columnDefinition = "TEXT")
    private String summaryAiCn;

    private String keywordsAi;
    private String categoryAi;
    private String sentimentAi;

    // New fields for user-generated articles
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_user_generated")
    private Boolean userGenerated = false;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    // Add relationship to User (optional, for JPA convenience)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}