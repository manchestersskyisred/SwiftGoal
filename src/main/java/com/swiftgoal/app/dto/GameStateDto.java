package com.swiftgoal.app.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDto {
    private Long secretPlayerId; // 神秘球员ID
    private int attemptsMade; // 已尝试次数
    private int maxAttempts; // 最大尝试次数
    private List<GuessResultDto> previousGuesses; // 之前的猜测结果
    private boolean gameOver; // 游戏是否结束
    private boolean won; // 是否获胜
    private String gameMode; // 游戏模式：DAILY_CHALLENGE 或 UNLIMITED
} 