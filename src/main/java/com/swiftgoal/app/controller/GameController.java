package com.swiftgoal.app.controller;

import com.swiftgoal.app.dto.GameStateDto;
import com.swiftgoal.app.dto.GuessResultDto;
import com.swiftgoal.app.dto.PlayerSearchResultDto;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerContract;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.football.PlayerContractRepository;
import com.swiftgoal.app.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
@RequestMapping("/game")
public class GameController {

    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "NBA", "英超", "西甲", "德甲", "法甲", "意甲", "沙特联", "女足", "MLB", "网球", "综合体育"
    );

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/demo")
    public String showGameDemoPage(Model model) {
        // This method is purely for serving the static demo page.
        // All data is hardcoded in the HTML itself for demonstration purposes.
        return "game_demo";
    }

    @GetMapping("/play")
    public String showGamePage(Model model, HttpSession session) {
        // 检查是否有活跃的游戏
        GameStateDto gameState = (GameStateDto) session.getAttribute("gameState");
        if (gameState == null) {
            // 默认开始每日挑战模式
            gameState = gameService.startNewGame("DAILY_CHALLENGE");
            session.setAttribute("gameState", gameState);
        }
        
        model.addAttribute("gameState", gameState);

        // Add data for sidebar
        model.addAttribute("categories", ALL_CATEGORIES);
        model.addAttribute("currentCategory", "猜球员游戏");
        
        return "game";
    }

    @PostMapping("/api/start")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> startNewGame(@RequestBody StartGameRequest request, HttpSession session) {
        try {
            // 验证游戏模式
            if (!"DAILY_CHALLENGE".equals(request.getMode()) && !"UNLIMITED".equals(request.getMode())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid game mode");
                errorResponse.put("message", "游戏模式无效");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            GameStateDto gameState = gameService.startNewGame(request.getMode());
            session.setAttribute("gameState", gameState);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameState", gameState);
            response.put("message", "游戏已开始");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start game");
            errorResponse.put("message", "开始游戏失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/api/guess")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> makeGuess(@RequestBody GuessRequest request, HttpSession session) {
        try {
            GameStateDto gameState = (GameStateDto) session.getAttribute("gameState");
            if (gameState == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No active game");
                errorResponse.put("message", "没有活跃的游戏");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (gameState.isGameOver()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Game is over");
                errorResponse.put("message", "游戏已结束");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            GuessResultDto result = gameService.makeGuess(gameState.getSecretPlayerId(), request.getPlayerId(), gameState);
            session.setAttribute("gameState", gameState);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("gameState", gameState);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid player");
            errorResponse.put("message", "球员不存在");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("message", "服务器错误：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/api/players/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchPlayers(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("players", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            List<PlayerSearchResultDto> players = gameService.searchPlayers(query);
            
            // 转换为前端友好的结构
            List<Map<String, Object>> playerList = new ArrayList<>();
            for (PlayerSearchResultDto player : players) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", player.getId());
                map.put("displayName", player.getDisplayName());
                map.put("fullNameEn", player.getFullNameEn());
                map.put("fullNameCn", player.getFullNameCn());
                map.put("nationality", player.getDisplayNationality());
                map.put("position", player.getDisplayPosition());
                map.put("dateOfBirth", player.getDateOfBirth());
                map.put("photoUrl", player.getPhotoUrl());
                playerList.add(map);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("players", playerList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Search failed");
            errorResponse.put("message", "搜索失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/api/players/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPlayerDetails(@PathVariable Long id) {
        try {
            Map<String, Object> playerDetails = gameService.getHardcodedPlayerDetails(id);
            if (playerDetails == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Player not found");
                errorResponse.put("message", "球员不存在");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("player", playerDetails);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get player details");
            errorResponse.put("message", "获取球员详情失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/api/state")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGameState(HttpSession session) {
        GameStateDto gameState = (GameStateDto) session.getAttribute("gameState");
        if (gameState == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No game state");
            errorResponse.put("message", "没有游戏状态");
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("gameState", gameState);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/answer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAnswer(HttpSession session) {
        GameStateDto gameState = (GameStateDto) session.getAttribute("gameState");
        if (gameState == null || gameState.isGameOver()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No active game or game over");
            errorResponse.put("message", "游戏尚未开始或已结束");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        PlayerSearchResultDto player = gameService.getCorrectAnswer(gameState.getSecretPlayerId());

        if (player == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Answer not found");
            errorResponse.put("message", "无法找到答案");
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("player", player);
        return ResponseEntity.ok(response);
    }

    // 内部类用于请求体
    public static class StartGameRequest {
        private String mode;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class GuessRequest {
        private Long playerId;

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }
    }

    // PlayerDetailDto is no longer needed here as the PlayerSearchResultDto from the service is sufficient
} 