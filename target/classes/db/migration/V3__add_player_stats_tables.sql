CREATE TABLE player_season_stats (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    season VARCHAR(50) NOT NULL,         -- 如: '2023-2024'
    competition_name VARCHAR(255) NOT NULL, -- 如: 'Premier League', 'Champions League'

    -- 核心表现数据
    matches_played INT DEFAULT 0,
    matches_started INT DEFAULT 0,
    minutes_played INT DEFAULT 0,
    goals INT DEFAULT 0,
    assists INT DEFAULT 0,
    yellow_cards INT DEFAULT 0,
    red_cards INT DEFAULT 0,

    -- 更多高阶数据 (可选，来自FBref)
    expected_goals REAL, -- 用 REAL 或 NUMERIC 类型
    expected_assists REAL,

    -- 外键约束
    CONSTRAINT fk_player_season FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_season FOREIGN KEY(team_id) REFERENCES teams(id) ON DELETE CASCADE,

    -- 联合唯一约束，防止同一球员在同一赛季、同一赛事有重复记录
    UNIQUE (player_id, season, competition_name)
);

CREATE TABLE player_match_stats (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    match_date DATE NOT NULL,
    competition_name VARCHAR(255) NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    home_team_score INT,
    away_team_score INT,

    -- 球员本场表现
    minutes_played INT,
    goals INT DEFAULT 0,
    assists INT DEFAULT 0,
    shots INT,
    shots_on_target INT,
    key_passes INT,
    player_rating REAL,             -- 球员评分 (来自API)
    heatmap_data JSONB,             -- 热区图数据 (JSONB类型，用于存储来自API的坐标或图片URL)

    -- 外键约束
    CONSTRAINT fk_player_match FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_home_team FOREIGN KEY(home_team_id) REFERENCES teams(id),
    CONSTRAINT fk_away_team FOREIGN KEY(away_team_id) REFERENCES teams(id)
);

-- 为常用查询创建索引
CREATE INDEX idx_match_stats_player_date ON player_match_stats(player_id, match_date DESC); 