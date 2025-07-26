package com.swiftgoal.app.service;

import com.swiftgoal.app.dto.TranslationResultDto;
import com.swiftgoal.app.repository.entity.football.League;
import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.Team;
import com.swiftgoal.app.repository.football.LeagueRepository;
import com.swiftgoal.app.repository.football.PlayerRepository;
import com.swiftgoal.app.repository.football.TeamRepository;
import com.swiftgoal.app.service.DqdTranslatorScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ChineseNameTranslationService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final DqdTranslatorScraper dqdScraper;

    // 预定义的翻译映射
    private static final Map<String, String> PLAYER_NAME_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> TEAM_NAME_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> LEAGUE_NAME_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> NATIONALITY_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> POSITION_TRANSLATIONS = new HashMap<>();
    private static final Map<String, String> COUNTRY_TRANSLATIONS = new HashMap<>();

    static {
        // 知名球员翻译
        PLAYER_NAME_TRANSLATIONS.put("Lionel Messi", "梅西");
        PLAYER_NAME_TRANSLATIONS.put("Cristiano Ronaldo", "克里斯蒂亚诺·罗纳尔多");
        PLAYER_NAME_TRANSLATIONS.put("Neymar", "内马尔");
        PLAYER_NAME_TRANSLATIONS.put("Kylian Mbappé", "姆巴佩");
        PLAYER_NAME_TRANSLATIONS.put("Erling Haaland", "哈兰德");
        PLAYER_NAME_TRANSLATIONS.put("Kevin De Bruyne", "德布劳内");
        PLAYER_NAME_TRANSLATIONS.put("Luka Modrić", "莫德里奇");
        PLAYER_NAME_TRANSLATIONS.put("Karim Benzema", "本泽马");
        PLAYER_NAME_TRANSLATIONS.put("Robert Lewandowski", "莱万多夫斯基");
        PLAYER_NAME_TRANSLATIONS.put("Mohamed Salah", "萨拉赫");
        PLAYER_NAME_TRANSLATIONS.put("Bruno Fernandes", "布鲁诺·费尔南德斯");
        PLAYER_NAME_TRANSLATIONS.put("Harry Kane", "哈里·凯恩");
        PLAYER_NAME_TRANSLATIONS.put("Vinícius Júnior", "维尼修斯");
        PLAYER_NAME_TRANSLATIONS.put("Jude Bellingham", "贝林厄姆");
        PLAYER_NAME_TRANSLATIONS.put("Phil Foden", "菲尔·福登");

        // 知名球队翻译
        TEAM_NAME_TRANSLATIONS.put("Manchester United", "曼彻斯特联");
        TEAM_NAME_TRANSLATIONS.put("Manchester City", "曼彻斯特城");
        TEAM_NAME_TRANSLATIONS.put("Arsenal", "阿森纳");
        TEAM_NAME_TRANSLATIONS.put("Liverpool", "利物浦");
        TEAM_NAME_TRANSLATIONS.put("Chelsea", "切尔西");
        TEAM_NAME_TRANSLATIONS.put("Tottenham Hotspur", "托特纳姆热刺");
        TEAM_NAME_TRANSLATIONS.put("Real Madrid", "皇家马德里");
        TEAM_NAME_TRANSLATIONS.put("Barcelona", "巴塞罗那");
        TEAM_NAME_TRANSLATIONS.put("Bayern Munich", "拜仁慕尼黑");
        TEAM_NAME_TRANSLATIONS.put("Borussia Dortmund", "多特蒙德");
        TEAM_NAME_TRANSLATIONS.put("Paris Saint-Germain", "巴黎圣日耳曼");
        TEAM_NAME_TRANSLATIONS.put("Juventus", "尤文图斯");
        TEAM_NAME_TRANSLATIONS.put("AC Milan", "AC米兰");
        TEAM_NAME_TRANSLATIONS.put("Inter Milan", "国际米兰");

        // 联赛翻译
        LEAGUE_NAME_TRANSLATIONS.put("Premier League", "英格兰足球超级联赛");
        LEAGUE_NAME_TRANSLATIONS.put("La Liga", "西班牙足球甲级联赛");
        LEAGUE_NAME_TRANSLATIONS.put("Bundesliga", "德国足球甲级联赛");
        LEAGUE_NAME_TRANSLATIONS.put("Serie A", "意大利足球甲级联赛");
        LEAGUE_NAME_TRANSLATIONS.put("Ligue 1", "法国足球甲级联赛");

        // 国籍翻译
        NATIONALITY_TRANSLATIONS.put("Argentina", "阿根廷");
        NATIONALITY_TRANSLATIONS.put("Portugal", "葡萄牙");
        NATIONALITY_TRANSLATIONS.put("Brazil", "巴西");
        NATIONALITY_TRANSLATIONS.put("France", "法国");
        NATIONALITY_TRANSLATIONS.put("Norway", "挪威");
        NATIONALITY_TRANSLATIONS.put("Belgium", "比利时");
        NATIONALITY_TRANSLATIONS.put("Croatia", "克罗地亚");
        NATIONALITY_TRANSLATIONS.put("Poland", "波兰");
        NATIONALITY_TRANSLATIONS.put("Egypt", "埃及");
        NATIONALITY_TRANSLATIONS.put("England", "英格兰");
        NATIONALITY_TRANSLATIONS.put("Spain", "西班牙");
        NATIONALITY_TRANSLATIONS.put("Germany", "德国");
        NATIONALITY_TRANSLATIONS.put("Italy", "意大利");
        NATIONALITY_TRANSLATIONS.put("Netherlands", "荷兰");
        NATIONALITY_TRANSLATIONS.put("Denmark", "丹麦");

        // 位置翻译
        POSITION_TRANSLATIONS.put("Forward", "前锋");
        POSITION_TRANSLATIONS.put("Midfielder", "中场");
        POSITION_TRANSLATIONS.put("Defender", "后卫");
        POSITION_TRANSLATIONS.put("Goalkeeper", "门将");
        POSITION_TRANSLATIONS.put("Attacking Midfield", "攻击型中场");
        POSITION_TRANSLATIONS.put("Central Midfield", "中前卫");
        POSITION_TRANSLATIONS.put("Defensive Midfield", "防守型中场");
        POSITION_TRANSLATIONS.put("Left Winger", "左边锋");
        POSITION_TRANSLATIONS.put("Right Winger", "右边锋");
        POSITION_TRANSLATIONS.put("Centre-Forward", "中锋");
        POSITION_TRANSLATIONS.put("Left-Back", "左后卫");
        POSITION_TRANSLATIONS.put("Right-Back", "右后卫");
        POSITION_TRANSLATIONS.put("Centre-Back", "中后卫");

        // 国家翻译
        COUNTRY_TRANSLATIONS.put("England", "英格兰");
        COUNTRY_TRANSLATIONS.put("Spain", "西班牙");
        COUNTRY_TRANSLATIONS.put("Germany", "德国");
        COUNTRY_TRANSLATIONS.put("Italy", "意大利");
        COUNTRY_TRANSLATIONS.put("France", "法国");
    }

    public ChineseNameTranslationService(PlayerRepository playerRepository,
                                       TeamRepository teamRepository,
                                       LeagueRepository leagueRepository,
                                       DqdTranslatorScraper dqdScraper) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.leagueRepository = leagueRepository;
        this.dqdScraper = dqdScraper;
    }

    /**
     * 为球员添加中文名，优先使用预定义翻译，失败则尝试爬虫
     */
    @Transactional
    public void translatePlayerNames() {
        log.info("开始为球员添加中文名...");
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            boolean updated = false;

            // 翻译球员姓名
            if (player.getFullNameCn() == null && player.getFullNameEn() != null) {
                String chineseName = PLAYER_NAME_TRANSLATIONS.get(player.getFullNameEn());
                if (chineseName != null) {
                    player.setFullNameCn(chineseName);
                    updated = true;
                    log.debug("为球员 {} 添加中文名 (预定义): {}", player.getFullNameEn(), chineseName);
                }
            }

            // 翻译国籍
            if (player.getNationalityCn() == null && player.getNationalityEn() != null) {
                String chineseNationality = NATIONALITY_TRANSLATIONS.get(player.getNationalityEn());
                if (chineseNationality != null) {
                    player.setNationalityCn(chineseNationality);
                    updated = true;
                    log.debug("为球员 {} 添加中文国籍: {}", player.getFullNameEn(), chineseNationality);
                }
            }

            // 翻译位置
            if (player.getPositionCn() == null && player.getPositionEn() != null) {
                String chinesePosition = POSITION_TRANSLATIONS.get(player.getPositionEn());
                if (chinesePosition != null) {
                    player.setPositionCn(chinesePosition);
                    updated = true;
                    log.debug("为球员 {} 添加中文位置: {}", player.getFullNameEn(), chinesePosition);
                }
            }

            if (updated) {
                playerRepository.save(player);
            }
        }
        log.info("球员中文名翻译完成");
    }

    /**
     * 为球队添加中文名
     */
    @Transactional
    public void translateTeamNames() {
        log.info("开始为球队添加中文名...");
        List<Team> teams = teamRepository.findAll();

        for (Team team : teams) {
            boolean updated = false;

            // 翻译球队名称
            if (team.getNameCn() == null && team.getNameEn() != null) {
                String chineseName = TEAM_NAME_TRANSLATIONS.get(team.getNameEn());
                if (chineseName != null) {
                    team.setNameCn(chineseName);
                    updated = true;
                    log.debug("为球队 {} 添加中文名: {}", team.getNameEn(), chineseName);
                }
            }

            // 翻译国家
            if (team.getCountryCn() == null && team.getCountryEn() != null) {
                String chineseCountry = COUNTRY_TRANSLATIONS.get(team.getCountryEn());
                if (chineseCountry != null) {
                    team.setCountryCn(chineseCountry);
                    updated = true;
                    log.debug("为球队 {} 添加中文国家: {}", team.getNameEn(), chineseCountry);
                }
            }

            if (updated) {
                teamRepository.save(team);
            }
        }
        log.info("球队中文名翻译完成");
    }

    /**
     * 为联赛添加中文名
     */
    @Transactional
    public void translateLeagueNames() {
        log.info("开始为联赛添加中文名...");
        List<League> leagues = leagueRepository.findAll();

        for (League league : leagues) {
            boolean updated = false;

            // 翻译联赛名称
            if (league.getNameCn() == null && league.getNameEn() != null) {
                String chineseName = LEAGUE_NAME_TRANSLATIONS.get(league.getNameEn());
                if (chineseName != null) {
                    league.setNameCn(chineseName);
                    updated = true;
                    log.debug("为联赛 {} 添加中文名: {}", league.getNameEn(), chineseName);
                }
            }

            // 翻译国家
            if (league.getCountryCn() == null && league.getCountryEn() != null) {
                String chineseCountry = COUNTRY_TRANSLATIONS.get(league.getCountryEn());
                if (chineseCountry != null) {
                    league.setCountryCn(chineseCountry);
                    updated = true;
                    log.debug("为联赛 {} 添加中文国家: {}", league.getNameEn(), chineseCountry);
                }
            }

            if (updated) {
                leagueRepository.save(league);
            }
        }
        log.info("联赛中文名翻译完成");
    }

    /**
     * 执行所有翻译
     */
    @Transactional
    public void translateAllNames() {
        log.info("开始执行所有中文名翻译...");
        translateLeagueNames();
        translateTeamNames();
        translatePlayerNames();
        log.info("所有中文名翻译完成");
    }

    /**
     * [新增] 使用爬虫翻译单个球员，并更新数据库
     * @param playerId 球员ID
     * @return 是否翻译成功
     */
    @Transactional
    public boolean translateSinglePlayerWithScraper(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            log.warn("未找到ID为 {} 的球员", playerId);
            return false;
        }

        Player player = playerOpt.get();
        if (player.getFullNameCn() != null && !player.getFullNameCn().isBlank()) {
            log.info("球员 {} ({}) 已有中文名，无需翻译。", player.getFullNameEn(), player.getId());
            return true;
        }

        TranslationResultDto result = dqdScraper.translate(player.getFullNameEn());
        if (result != null && result.getPlayerNameCn() != null && !result.getPlayerNameCn().isBlank()) {
            player.setFullNameCn(result.getPlayerNameCn());
            playerRepository.save(player);
            log.info("成功为球员 {} ({}) 爬取并更新中文名: {}", player.getFullNameEn(), player.getId(), result.getPlayerNameCn());

            // 顺便更新球队中文名
            if (result.getTeamNameCn() != null && !result.getTeamNameCn().isBlank()) {
                updateTeamChineseName(player, result.getTeamNameCn());
            }
            return true;
        } else {
            log.warn("未能为球员 {} ({}) 爬取到中文名。", player.getFullNameEn(), player.getId());
            return false;
        }
    }

    /**
     * [新增] 批量使用爬虫翻译所有无中文名的球员
     */
    @Transactional
    public void translateAllPlayersWithScraper() {
        log.info("开始使用懂球帝爬虫批量翻译球员中文名...");
        List<Player> playersToTranslate = playerRepository.findAll().stream()
                .filter(p -> p.getFullNameCn() == null || p.getFullNameCn().isBlank())
                .toList();

        log.info("发现 {} 名球员需要翻译。", playersToTranslate.size());

        for (Player player : playersToTranslate) {
            translateSinglePlayerWithScraper(player.getId());
        }
        log.info("球员爬虫翻译任务完成。");
    }


    private void updateTeamChineseName(Player player, String teamNameCn) {
        // 这是一个简化逻辑，需要一个更健壮的方式来关联球员和他们当前的球队合同
        // 此处仅为演示
        log.info("尝试为球员 {} 的球队更新中文名为 {}", player.getFullNameEn(), teamNameCn);
    }

    /**
     * 根据英文名查找中文翻译
     */
    public String getPlayerChineseName(String englishName) {
        return PLAYER_NAME_TRANSLATIONS.get(englishName);
    }

    public String getTeamChineseName(String englishName) {
        return TEAM_NAME_TRANSLATIONS.get(englishName);
    }

    public String getLeagueChineseName(String englishName) {
        return LEAGUE_NAME_TRANSLATIONS.get(englishName);
    }
} 