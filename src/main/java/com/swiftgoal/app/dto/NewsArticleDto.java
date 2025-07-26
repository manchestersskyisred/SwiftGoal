package com.swiftgoal.app.dto;

import com.swiftgoal.app.repository.entity.NewsArticle;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsArticleDto {
    private Long id;
    private String titleCn;
    private String url;
    private String source;
    private LocalDateTime publishDate;
    private String summaryAiCn;
    private String categoryAi;
    private int likesCount;
    private int commentsCount;

    public static NewsArticleDto fromEntity(NewsArticle entity) {
        return NewsArticleDto.builder()
                .id(entity.getId())
                .titleCn(entity.getTitleCn() != null ? entity.getTitleCn() : entity.getTitle())
                .url(entity.getUrl())
                .source(entity.getSource())
                .publishDate(entity.getPublishDate())
                .summaryAiCn(entity.getSummaryAiCn() != null ? entity.getSummaryAiCn() : entity.getSummaryAi())
                .categoryAi(entity.getCategoryAi())
                .likesCount(entity.getLikes() != null ? entity.getLikes().size() : 0)
                .commentsCount(entity.getComments() != null ? entity.getComments().size() : 0)
                .build();
    }
} 