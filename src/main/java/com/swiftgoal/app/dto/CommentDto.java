package com.swiftgoal.app.dto;

import com.swiftgoal.app.repository.entity.ArticleComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private String username;
    private String avatarUrl;
    private Long parentId;
    private LocalDateTime createdAt;
    private Set<CommentDto> replies;

    public static CommentDto fromEntity(ArticleComment entity) {
        if (entity == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUsername(entity.getUser().getUsername());
            dto.setAvatarUrl(entity.getUser().getAvatarUrl());
        }

        if (entity.getParentComment() != null) {
            dto.setParentId(entity.getParentComment().getId());
        }

        if (entity.getReplies() != null && !entity.getReplies().isEmpty()) {
            dto.setReplies(entity.getReplies().stream()
                    .sorted(Comparator.comparing(ArticleComment::getCreatedAt)) // Sort replies ascending
                    .map(CommentDto::fromEntity)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
} 