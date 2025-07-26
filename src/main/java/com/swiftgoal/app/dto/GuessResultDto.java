package com.swiftgoal.app.dto;

import com.swiftgoal.app.repository.entity.football.Player;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuessResultDto {
    private Player guessedPlayer; // 猜测的球员信息
    private Feedback nationalityFeedback; // 国籍的反馈
    private Feedback leagueFeedback; // 联赛的反馈
    private Feedback clubFeedback; // 俱乐部的反馈
    private Feedback positionFeedback; // 位置的反馈
    private Feedback ageFeedback; // 年龄的反馈
    private Feedback numberFeedback; // 号码的反馈
    private Feedback valueFeedback; // 身价的反馈
    private boolean isCorrect; // 是否完全猜对
    private int attemptNumber; // 第几次尝试
} 