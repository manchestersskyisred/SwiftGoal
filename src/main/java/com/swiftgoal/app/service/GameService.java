package com.swiftgoal.app.service;

import com.swiftgoal.app.dto.GameStateDto;
import com.swiftgoal.app.dto.GuessResultDto;
import com.swiftgoal.app.dto.PlayerSearchResultDto;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.football.PlayerContractRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.time.LocalDateTime;
import com.swiftgoal.app.dto.Feedback;
import java.util.HashMap;
import java.util.Map;


@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final PlayerRepository playerRepository;
    private final PlayerContractRepository playerContractRepository;
    private final ChineseNameTranslationService chineseNameTranslationService;

    // --- Hardcoded Player Data ---
    private static final List<HardcodedPlayer> hardcodedPlayers;

    static {
        hardcodedPlayers = Arrays.asList(
            new HardcodedPlayer(1, "基利安·姆巴佩", "法国", "西甲", "皇家马德里", 9, "前锋", 26, 180000000L),
            new HardcodedPlayer(2, "埃尔林·哈兰德", "挪威", "英超", "曼城", 9, "前锋", 24, 180000000L),
            new HardcodedPlayer(3, "维尼修斯·儒尼奥尔", "巴西", "西甲", "皇家马德里", 7, "前锋", 24, 180000000L),
            new HardcodedPlayer(4, "裘德·贝林厄姆", "英格兰", "西甲", "皇家马德里", 5, "中场", 22, 180000000L),
            new HardcodedPlayer(5, "布卡约·萨卡", "英格兰", "英超", "阿森纳", 7, "前锋", 23, 140000000L),
            new HardcodedPlayer(6, "贾马尔·穆西亚拉", "德国", "德甲", "拜仁慕尼黑", 42, "中场", 22, 130000000L),
            new HardcodedPlayer(7, "弗洛里安·维尔茨", "德国", "德甲", "拜耳勒沃库森", 27, "中场", 22, 130000000L),
            new HardcodedPlayer(8, "菲尔·福登", "英格兰", "英超", "曼城", 47, "中场", 25, 130000000L),
            new HardcodedPlayer(9, "罗德里", "西班牙", "英超", "曼城", 16, "中场", 29, 120000000L),
            new HardcodedPlayer(10, "德克兰·赖斯", "英格兰", "英超", "阿森纳", 41, "中场", 26, 120000000L),
            new HardcodedPlayer(11, "马丁·厄德高", "挪威", "英超", "阿森纳", 8, "中场", 26, 110000000L),
            new HardcodedPlayer(12, "朱利安·阿尔瓦雷斯", "阿根廷", "西甲", "马德里竞技", 19, "前锋", 25, 110000000L),
            new HardcodedPlayer(13, "费德里科·巴尔韦德", "乌拉圭", "西甲", "皇家马德里", 8, "中场", 27, 110000000L),
            new HardcodedPlayer(14, "爱德华多·卡马文加", "法国", "西甲", "皇家马德里", 12, "中场", 22, 100000000L),
            new HardcodedPlayer(15, "加维", "西班牙", "西甲", "巴塞罗那", 6, "中场", 20, 90000000L),
            new HardcodedPlayer(16, "奥雷利安·琼阿梅尼", "法国", "西甲", "皇家马德里", 18, "中场", 25, 90000000L),
            new HardcodedPlayer(17, "罗德里戈", "巴西", "西甲", "皇家马德里", 11, "前锋", 24, 90000000L),
            new HardcodedPlayer(18, "拉斐尔·莱奥", "葡萄牙", "意甲", "AC米兰", 10, "前锋", 26, 90000000L),
            new HardcodedPlayer(19, "劳塔罗·马丁内斯", "阿根廷", "意甲", "国际米兰", 10, "前锋", 27, 90000000L),
            new HardcodedPlayer(20, "加布里埃尔·马丁内利", "巴西", "英超", "阿森纳", 11, "前锋", 24, 85000000L),
            new HardcodedPlayer(21, "布鲁诺·吉马良斯", "巴西", "英超", "纽卡斯尔联", 39, "中场", 27, 85000000L),
            new HardcodedPlayer(22, "莫伊塞斯·凯塞多", "厄瓜多尔", "英超", "切尔西", 25, "中场", 23, 80000000L),
            new HardcodedPlayer(23, "恩佐·费尔南德斯", "阿根廷", "英超", "切尔西", 8, "中场", 24, 80000000L),
            new HardcodedPlayer(24, "维克多·奥斯梅恩", "尼日利亚", "意甲", "那不勒斯", 9, "前锋", 26, 80000000L),
            new HardcodedPlayer(25, "赫维恰·克瓦拉茨赫利亚", "格鲁吉亚", "意甲", "那不勒斯", 77, "前锋", 24, 80000000L),
            new HardcodedPlayer(26, "威廉·萨利巴", "法国", "英超", "阿森纳", 2, "后卫", 24, 80000000L),
            new HardcodedPlayer(27, "鲁本·迪亚斯", "葡萄牙", "英超", "曼城", 3, "后卫", 28, 80000000L),
            new HardcodedPlayer(28, "特伦特·亚历山大-阿诺德", "英格兰", "英超", "利物浦", 66, "后卫", 26, 75000000L),
            new HardcodedPlayer(29, "多米尼克·索博斯洛伊", "匈牙利", "英超", "利物浦", 8, "中场", 24, 75000000L),
            new HardcodedPlayer(30, "拉明·亚马尔", "西班牙", "西甲", "巴塞罗那", 27, "前锋", 18, 200000000L)
        );
    }


    public GameService(PlayerRepository playerRepository,
                       PlayerContractRepository playerContractRepository,
                       ChineseNameTranslationService chineseNameTranslationService) {
        this.playerRepository = playerRepository;
        this.playerContractRepository = playerContractRepository;
        this.chineseNameTranslationService = chineseNameTranslationService;
    }


    public GameStateDto startNewGame(String mode) {
        HardcodedPlayer secretPlayer = hardcodedPlayers.get(ThreadLocalRandom.current().nextInt(hardcodedPlayers.size()));
        
        GameStateDto newGame = new GameStateDto();
        newGame.setGameMode(mode);
        newGame.setSecretPlayerId(secretPlayer.getRank()); // Use rank as unique ID
        newGame.setAttemptsMade(0);
        newGame.setMaxAttempts(8);
        newGame.setGameOver(false);
        newGame.setWon(false);
        newGame.setPreviousGuesses(new ArrayList<>());
        
        return newGame;
    }

    @Transactional(readOnly = true)
    public List<PlayerSearchResultDto> searchPlayers(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return hardcodedPlayers.stream()
                .filter(p -> p.getChineseName().toLowerCase().contains(lowerCaseQuery))
                .map(p -> new PlayerSearchResultDto(
                    p.getRank(), // id
                    p.getChineseName(), // displayName
                    p.getChineseName(), // fullNameEn
                    p.getChineseName(), // fullNameCn
                    p.getNationality(), // nationalityEn
                    p.getNationality(), // nationalityCn
                    p.getPosition(), // positionEn
                    p.getPosition(), // positionCn
                    String.valueOf(p.getAge()), // dateOfBirth (using age as a string)
                    "" // photoUrl
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GuessResultDto makeGuess(long secretPlayerId, long guessedPlayerId, GameStateDto gameState) {
        Optional<HardcodedPlayer> secretPlayerOpt = hardcodedPlayers.stream().filter(p -> p.getRank() == secretPlayerId).findFirst();
        Optional<HardcodedPlayer> guessedPlayerOpt = hardcodedPlayers.stream().filter(p -> p.getRank() == guessedPlayerId).findFirst();

        if (secretPlayerOpt.isEmpty() || guessedPlayerOpt.isEmpty()) {
            throw new IllegalArgumentException("Player not found in hardcoded list.");
        }
        
        HardcodedPlayer secretPlayer = secretPlayerOpt.get();
        HardcodedPlayer guessedPlayer = guessedPlayerOpt.get();

        gameState.setAttemptsMade(gameState.getAttemptsMade() + 1);

        GuessResultDto result = new GuessResultDto();
        result.setAttemptNumber(gameState.getAttemptsMade());
        
        // Create a dummy Player entity for the DTO
        Player dummyPlayer = new Player();
        dummyPlayer.setId(guessedPlayer.getRank());
        dummyPlayer.setFullNameCn(guessedPlayer.getChineseName());
        dummyPlayer.setNationalityEn(guessedPlayer.getNationality());
        dummyPlayer.setPositionEn(guessedPlayer.getPosition());
        result.setGuessedPlayer(dummyPlayer);


        // Perform comparisons
        result.setNationalityFeedback(secretPlayer.getNationality().equals(guessedPlayer.getNationality()) ? Feedback.CORRECT : Feedback.WRONG);
        result.setLeagueFeedback(secretPlayer.getLeague().equals(guessedPlayer.getLeague()) ? Feedback.CORRECT : Feedback.WRONG);
        result.setClubFeedback(secretPlayer.getClub().equals(guessedPlayer.getClub()) ? Feedback.CORRECT : Feedback.WRONG);
        result.setPositionFeedback(secretPlayer.getPosition().equals(guessedPlayer.getPosition()) ? Feedback.CORRECT : Feedback.WRONG);
        
        result.setAgeFeedback(getComparisonFeedback(secretPlayer.getAge(), guessedPlayer.getAge()));
        result.setNumberFeedback(getComparisonFeedback(secretPlayer.getNumber(), guessedPlayer.getNumber()));
        result.setValueFeedback(getComparisonFeedback(secretPlayer.getValue(), guessedPlayer.getValue()));

        boolean isCorrect = secretPlayer.getRank() == guessedPlayer.getRank();
        result.setCorrect(isCorrect);

        if (isCorrect) {
            gameState.setWon(true);
            gameState.setGameOver(true);
        } else if (gameState.getAttemptsMade() >= gameState.getMaxAttempts()) {
            gameState.setGameOver(true);
        }

        gameState.getPreviousGuesses().add(result);
        return result;
    }
    
    public PlayerSearchResultDto getCorrectAnswer(long secretPlayerId) {
        return hardcodedPlayers.stream()
            .filter(p -> p.getRank() == secretPlayerId)
            .map(p -> new PlayerSearchResultDto(
                p.getRank(),
                p.getChineseName(),
                p.getChineseName(),
                p.getChineseName(),
                p.getNationality(),
                p.getNationality(),
                p.getPosition(),
                p.getPosition(),
                String.valueOf(p.getAge()),
                ""
            ))
            .findFirst()
            .orElse(null);
    }

    private Feedback getComparisonFeedback(long secret, long guessed) {
        if (secret == guessed) return Feedback.CORRECT;
        return secret > guessed ? Feedback.HIGHER : Feedback.LOWER;
    }

    public Map<String, Object> getHardcodedPlayerDetails(long id) {
        return hardcodedPlayers.stream()
            .filter(p -> p.getRank() == id)
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getRank());
                map.put("fullName", p.getChineseName());
                map.put("nationality", p.getNationality());
                map.put("leagueName", p.getLeague());
                map.put("clubName", p.getClub());
                map.put("position", p.getPosition());
                map.put("age", p.getAge());
                map.put("jerseyNumber", p.getNumber());
                map.put("marketValueEur", p.getValue());
                return map;
            })
            .findFirst()
            .orElse(null);
    }

    // A simple DTO for hardcoded player data
    static class HardcodedPlayer {
        private final long rank;
        private final String chineseName;
        private final String nationality;
        private final String league;
        private final String club;
        private final int number;
        private final String position;
        private final int age;
        private final long value;

        public HardcodedPlayer(long rank, String chineseName, String nationality, String league, String club, int number, String position, int age, long value) {
            this.rank = rank; this.chineseName = chineseName; this.nationality = nationality;
            this.league = league; this.club = club; this.number = number; this.position = position; this.age = age; this.value = value;
        }

        // Getters
        public long getRank() { return rank; }
        public String getChineseName() { return chineseName; }
        public String getNationality() { return nationality; }
        public String getLeague() { return league; }
        public String getClub() { return club; }
        public int getNumber() { return number; }
        public String getPosition() { return position; }
        public int getAge() { return age; }
        public long getValue() { return value; }
    }
} 