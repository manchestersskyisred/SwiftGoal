package com.swiftgoal.app.service;

import com.swiftgoal.app.dto.Feedback;
import com.swiftgoal.app.dto.GuessResultDto;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerContract;
import com.swiftgoal.app.repository.football.PlayerContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerComparisonService {

    private final PlayerContractRepository playerContractRepository;
    
    // 位置分组映射
    private static final Map<String, String> POSITION_GROUPS = new HashMap<>();
    
    static {
        // 前锋
        POSITION_GROUPS.put("ST", "Forward");
        POSITION_GROUPS.put("CF", "Forward");
        POSITION_GROUPS.put("LW", "Forward");
        POSITION_GROUPS.put("RW", "Forward");
        POSITION_GROUPS.put("SS", "Forward");
        
        // 中场
        POSITION_GROUPS.put("CM", "Midfielder");
        POSITION_GROUPS.put("CDM", "Midfielder");
        POSITION_GROUPS.put("CAM", "Midfielder");
        POSITION_GROUPS.put("LM", "Midfielder");
        POSITION_GROUPS.put("RM", "Midfielder");
        
        // 后卫
        POSITION_GROUPS.put("CB", "Defender");
        POSITION_GROUPS.put("LB", "Defender");
        POSITION_GROUPS.put("RB", "Defender");
        POSITION_GROUPS.put("LWB", "Defender");
        POSITION_GROUPS.put("RWB", "Defender");
        
        // 门将
        POSITION_GROUPS.put("GK", "Goalkeeper");
    }

    public PlayerComparisonService(PlayerContractRepository playerContractRepository) {
        this.playerContractRepository = playerContractRepository;
    }

    @Transactional(readOnly = true)
    public GuessResultDto comparePlayers(Player secretPlayer, Player guessedPlayer, int attemptNumber) {
        GuessResultDto result = new GuessResultDto();
        result.setGuessedPlayer(guessedPlayer);
        result.setAttemptNumber(attemptNumber);
        
        // 比较国籍
        result.setNationalityFeedback(compareNationality(secretPlayer, guessedPlayer));
        
        // 比较联赛
        result.setLeagueFeedback(compareLeague(secretPlayer, guessedPlayer));
        
        // 比较俱乐部
        result.setClubFeedback(compareClub(secretPlayer, guessedPlayer));
        
        // 比较位置
        result.setPositionFeedback(comparePosition(secretPlayer, guessedPlayer));
        
        // 比较年龄
        result.setAgeFeedback(compareAge(secretPlayer, guessedPlayer));
        
        // 比较号码
        result.setNumberFeedback(compareNumber(secretPlayer, guessedPlayer));
        
        // 比较身价
        result.setValueFeedback(compareValue(secretPlayer, guessedPlayer));
        
        // 检查是否完全猜对
        result.setCorrect(isFullyCorrect(result));
        
        return result;
    }
    
    private Feedback compareNationality(Player secret, Player guessed) {
        if (secret.getNationality() == null || guessed.getNationality() == null) {
            return Feedback.WRONG;
        }
        return secret.getNationality().equalsIgnoreCase(guessed.getNationality()) 
            ? Feedback.CORRECT : Feedback.WRONG;
    }
    
    private Feedback compareLeague(Player secret, Player guessed) {
        // 通过球员合同获取联赛信息
        PlayerContract secretContract = playerContractRepository.findByPlayer(secret).stream().findFirst().orElse(null);
        PlayerContract guessedContract = playerContractRepository.findByPlayer(guessed).stream().findFirst().orElse(null);
        
        if (secretContract == null || guessedContract == null || 
            secretContract.getTeam() == null || guessedContract.getTeam() == null ||
            secretContract.getTeam().getLeague() == null || guessedContract.getTeam().getLeague() == null) {
            return Feedback.WRONG;
        }
        
        String secretLeague = secretContract.getTeam().getLeague().getName();
        String guessedLeague = guessedContract.getTeam().getLeague().getName();
        
        return secretLeague.equalsIgnoreCase(guessedLeague) ? Feedback.CORRECT : Feedback.WRONG;
    }
    
    private Feedback compareClub(Player secret, Player guessed) {
        PlayerContract secretContract = playerContractRepository.findByPlayer(secret).stream().findFirst().orElse(null);
        PlayerContract guessedContract = playerContractRepository.findByPlayer(guessed).stream().findFirst().orElse(null);
        
        if (secretContract == null || guessedContract == null || 
            secretContract.getTeam() == null || guessedContract.getTeam() == null) {
            return Feedback.WRONG;
        }
        
        String secretClub = secretContract.getTeam().getName();
        String guessedClub = guessedContract.getTeam().getName();
        
        return secretClub.equalsIgnoreCase(guessedClub) ? Feedback.CORRECT : Feedback.WRONG;
    }
    
    private Feedback comparePosition(Player secret, Player guessed) {
        if (secret.getPosition() == null || guessed.getPosition() == null) {
            return Feedback.WRONG;
        }
        
        // 完全匹配
        if (secret.getPosition().equalsIgnoreCase(guessed.getPosition())) {
            return Feedback.CORRECT;
        }
        
        // 位置类别匹配
        String secretGroup = getPositionGroup(secret.getPosition());
        String guessedGroup = getPositionGroup(guessed.getPosition());
        
        if (secretGroup != null && secretGroup.equals(guessedGroup)) {
            return Feedback.PARTIAL;
        }
        
        return Feedback.WRONG;
    }
    
    private Feedback compareAge(Player secret, Player guessed) {
        int secretAge = calculateAge(secret.getDateOfBirth());
        int guessedAge = calculateAge(guessed.getDateOfBirth());
        
        if (secretAge == guessedAge) {
            return Feedback.CORRECT;
        } else if (secretAge > guessedAge) {
            return Feedback.HIGHER;
        } else {
            return Feedback.LOWER;
        }
    }
    
    private Feedback compareNumber(Player secret, Player guessed) {
        PlayerContract secretContract = playerContractRepository.findByPlayer(secret).stream().findFirst().orElse(null);
        PlayerContract guessedContract = playerContractRepository.findByPlayer(guessed).stream().findFirst().orElse(null);
        
        if (secretContract == null || guessedContract == null || 
            secretContract.getJerseyNumber() == null || guessedContract.getJerseyNumber() == null) {
            return Feedback.WRONG;
        }
        
        int secretNumber = secretContract.getJerseyNumber();
        int guessedNumber = guessedContract.getJerseyNumber();
        
        if (secretNumber == guessedNumber) {
            return Feedback.CORRECT;
        } else if (secretNumber > guessedNumber) {
            return Feedback.HIGHER;
        } else {
            return Feedback.LOWER;
        }
    }
    
    private Feedback compareValue(Player secret, Player guessed) {
        PlayerContract secretContract = playerContractRepository.findByPlayer(secret).stream().findFirst().orElse(null);
        PlayerContract guessedContract = playerContractRepository.findByPlayer(guessed).stream().findFirst().orElse(null);
        
        if (secretContract == null || guessedContract == null || 
            secretContract.getMarketValueEur() == null || guessedContract.getMarketValueEur() == null) {
            return Feedback.WRONG;
        }
        
        Long secretValue = secretContract.getMarketValueEur();
        Long guessedValue = guessedContract.getMarketValueEur();
        
        if (secretValue.equals(guessedValue)) {
            return Feedback.CORRECT;
        } else if (secretValue > guessedValue) {
            return Feedback.HIGHER;
        } else {
            return Feedback.LOWER;
        }
    }
    
    private String getPositionGroup(String position) {
        if (position == null) return null;
        return POSITION_GROUPS.get(position.toUpperCase());
    }
    
    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    private boolean isFullyCorrect(GuessResultDto result) {
        return result.getNationalityFeedback() == Feedback.CORRECT &&
               result.getLeagueFeedback() == Feedback.CORRECT &&
               result.getClubFeedback() == Feedback.CORRECT &&
               result.getPositionFeedback() == Feedback.CORRECT &&
               result.getAgeFeedback() == Feedback.CORRECT &&
               result.getNumberFeedback() == Feedback.CORRECT &&
               result.getValueFeedback() == Feedback.CORRECT;
    }
} 