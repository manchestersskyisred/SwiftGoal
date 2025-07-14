package com.sportslens.ai.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "news_articles")
@Getter
@Setter
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(unique = true, length = 1024)
    private String url;

    private String source;
    private LocalDateTime publishDate;

    @Column(columnDefinition = "TEXT")
    private String rawContent;

    @Column(columnDefinition = "TEXT")
    private String rawHtmlContent;

    @Column(columnDefinition = "TEXT")
    private String translatedContent;

    @Column(columnDefinition = "TEXT")
    private String titleCn;

    @Column(columnDefinition = "TEXT")
    private String summaryAi;

    @Column(columnDefinition = "TEXT")
    private String summaryAiCn;

    private String keywordsAi;
    private String categoryAi;
    private String sentimentAi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "boolean default false", updatable = false)
    private boolean userGenerated = false;

    private LocalDateTime uploadTime;

    @Column(columnDefinition = "integer default 0")
    private Integer translationStatus = 0;

    @Column(columnDefinition = "integer default 1") // 0: Pending, 1: Approved, 2: Rejected
    private Integer moderationStatus = 1;

    @OneToMany(mappedBy = "newsArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BrowsingHistory> browsingHistory;

}