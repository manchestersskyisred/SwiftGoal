package com.swiftgoal.app.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {
    private String type; // "player" or "team"
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String imageUrl;
    private String detailUrl;
} 