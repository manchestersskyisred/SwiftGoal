package com.swiftgoal.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandingsRowDto {
    private int rank;
    private String teamName;
    private String teamLogoUrl;
    private int points;
    private int played;
    private int win;
    private int draw;
    private int lose;
    private int goalsFor;
    private int goalsAgainst;
    private int goalsDiff;
    private String form;
} 