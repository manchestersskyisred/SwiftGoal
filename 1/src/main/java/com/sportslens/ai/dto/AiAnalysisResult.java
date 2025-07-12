package com.sportslens.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResult {

    private String translatedTitle;
    private String chineseSummary;
    private String keywords;
    private String partition;
} 