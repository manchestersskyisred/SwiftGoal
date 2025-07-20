package com.swiftgoal.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiAnalysisResult {
    private String translatedTitle;
    private String chineseSummary;
    private String keywords;
    private String partition;
} 