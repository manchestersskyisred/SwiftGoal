package com.swiftgoal.app.controller;

import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.entity.football.Team;
import com.swiftgoal.app.repository.football.TeamRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

@Controller
@RequestMapping("/teams")
public class TeamController {

    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "NBA", "英超", "西甲", "德甲", "法甲", "意甲", "沙特联", "女足", "MLB", "网球", "综合体育"
    );

    // DTO for detailed player info for the demo
    public static class PlayerDetailDto {
        private final String name;
        private final int age;
        private final String nationality;
        private final String position;
        private final String contractUntil;
        public PlayerDetailDto(String name, int age, String nationality, String position, String contractUntil) {
            this.name = name; this.age = age; this.nationality = nationality; this.position = position; this.contractUntil = contractUntil;
        }
        // Getters
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getNationality() { return nationality; }
        public String getPosition() { return position; }
        public String getContractUntil() { return contractUntil; }
    }

    // DTO for competition stats for the demo
    public static class CompetitionStatDto {
        private final String name;
        private final String finalStage;
        private final String record;
        private final String winRate;
        private final String notes;
        public CompetitionStatDto(String name, String finalStage, String record, String winRate, String notes) {
            this.name = name; this.finalStage = finalStage; this.record = record; this.winRate = winRate; this.notes = notes;
        }
        // Getters
        public String getName() { return name; }
        public String getFinalStage() { return finalStage; }
        public String getRecord() { return record; }
        public String getWinRate() { return winRate; }
        public String getNotes() { return notes; }
    }


    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamController(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/{id}")
    public String teamDetail(@PathVariable Long id, Model model) {
        Optional<Team> teamOpt = teamRepository.findById(id);
        if (teamOpt.isEmpty()) {
            return "redirect:/";
        }
        Team team = teamOpt.get();

        // Add common data for header/footer first
        model.addAttribute("categories", ALL_CATEGORIES);

        // Check if the request is for the special Liverpool demo page
        if (id == 62) {
            // --- Start of Liverpool Fake Data Injection ---
            team.setLogoUrl("/images/news/liverpool_logo.jpeg");
            model.addAttribute("team", team);

            // Detailed player list
            Map<String, List<PlayerDetailDto>> categorizedPlayers = new HashMap<>();
            categorizedPlayers.put("守门员", Arrays.asList(
                    new PlayerDetailDto("Giorgi Mamardashvili", 24, "格鲁吉亚", "守门员", "2031"),
                    new PlayerDetailDto("Alisson", 32, "巴西", "守门员", "2027"),
                    new PlayerDetailDto("Freddie Woodman", 28, "英格兰", "守门员", "2026"),
                    new PlayerDetailDto("Ármin Pécsi", 20, "匈牙利", "守门员", "2030")
            ));
            categorizedPlayers.put("后卫", Arrays.asList(
                    new PlayerDetailDto("Ibrahima Konaté", 26, "法国", "后卫", "2026"),
                    new PlayerDetailDto("Virgil van Dijk", 34, "荷兰", "后卫", "2027"),
                    new PlayerDetailDto("Joe Gomez", 28, "英格兰", "后卫", "2027"),
                    new PlayerDetailDto("Rhys Williams", 24, "英格兰", "后卫", "2026"),
                    new PlayerDetailDto("Milos Kerkez", 21, "匈牙利", "后卫", "2029"),
                    new PlayerDetailDto("Konstantinos Tsimikas", 29, "希腊", "后卫", "2027"),
                    new PlayerDetailDto("Andrew Robertson", 31, "苏格兰", "后卫", "2026"),
                    new PlayerDetailDto("Jeremie Frimpong", 24, "荷兰", "后卫", "2030"),
                    new PlayerDetailDto("Conor Bradley", 22, "北爱尔兰", "后卫", "2029"),
                    new PlayerDetailDto("Calvin Ramsay", 21, "苏格兰", "后卫", "2027")
            ));
            categorizedPlayers.put("中场", Arrays.asList(
                    new PlayerDetailDto("Ryan Gravenberch", 23, "荷兰", "中场", "2028"),
                    new PlayerDetailDto("Stefan Bajcetic", 20, "西班牙", "中场", "2027"),
                    new PlayerDetailDto("Wataru Endo", 32, "日本", "中场", "2027"),
                    new PlayerDetailDto("Tyler Morton", 22, "英格兰", "中场", "2026"),
                    new PlayerDetailDto("Alexis Mac Allister", 26, "阿根廷", "中场", "2028"),
                    new PlayerDetailDto("Curtis Jones", 24, "英格兰", "中场", "2027"),
                    new PlayerDetailDto("Florian Wirtz", 22, "德国", "中场", "2030"),
                    new PlayerDetailDto("Dominik Szoboszlai", 24, "匈牙利", "中场", "2028"),
                    new PlayerDetailDto("Harvey Elliott", 22, "英格兰", "中场", "2027")
            ));
            categorizedPlayers.put("前锋", Arrays.asList(
                    new PlayerDetailDto("Luis Díaz", 28, "哥伦比亚", "前锋", "2027"),
                    new PlayerDetailDto("Cody Gakpo", 26, "荷兰", "前锋", "2028"),
                    new PlayerDetailDto("Mohamed Salah", 33, "埃及", "前锋", "2027"),
                    new PlayerDetailDto("Federico Chiesa", 27, "意大利", "前锋", "2028"),
                    new PlayerDetailDto("Ben Doak", 19, "苏格兰", "前锋", "2026"),
                    new PlayerDetailDto("Darwin Núñez", 26, "乌拉圭", "前锋", "2028")
            ));
            model.addAttribute("categorizedPlayers", categorizedPlayers);

            // Detailed season statistics
            List<CompetitionStatDto> competitionStats = Arrays.asList(
                    new CompetitionStatDto("英超 (Premier League)", "冠军，第1名", "38-25-9-4-86-41-+45", "65.79%", "2025年4月27日以5-1战胜热刺后提前夺冠，Mohamed Salah打进29球，创个人纪录"),
                    new CompetitionStatDto("足总杯 (FA Cup)", "第四轮", "2-1-0-1-4-1-+3", "50.00%", "第三轮进入，2月9日以1-0负于普利茅斯阿根廷被淘汰"),
                    new CompetitionStatDto("联赛杯 (EFL Cup)", "决赛 (亚军)", "6-4-0-2-15-7-+8", "66.67%", "第三轮进入，3月16日决赛以1-2负于纽卡斯尔联"),
                    new CompetitionStatDto("欧冠 (UEFA Champions League)", "十六强", "10-8-0-2-18-6-+12", "80.00%", "小组赛第一出线，3月11日以1-1 (点球4-1) 负于巴黎圣日耳曼被淘汰")
            );
            model.addAttribute("competitionStats", competitionStats);

            // Special notes
            model.addAttribute("specialNotes", "值得注意的是，2025-2026赛季是自2015-2016赛季以来首次没有Trent Alexander-Arnold（已于2025年6月1日转会至皇家马德里），也是自2019-2020赛季以来首次没有Diogo Jota（于2025年7月3日因车祸去世）。为纪念Jota，球队于2025年7月11日退役了他的20号球衣。");
            model.addAttribute("dataSource", "来源：Transfermarkt - Liverpool FC Squad, Wikipedia, NBC Sports");


            // Add specific category for the header
            model.addAttribute("currentCategory", "英超");
            // --- End of Fake Data Injection ---

            return "liverpool_demo"; // Return the dedicated Liverpool demo template
        } else {
            // Standard logic for all other teams
            model.addAttribute("team", team);
            List<Player> players = playerRepository.findByTeam_Id(id);
            model.addAttribute("players", players);
            
            model.addAttribute("currentCategory", team.getLeague() != null ? team.getLeague().getNameEn() : "Teams");

            return "team_detail"; // Return the generic team template
        }
    }
} 