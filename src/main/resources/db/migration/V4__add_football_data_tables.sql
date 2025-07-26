CREATE TABLE leagues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- 如: 'Premier League'
    country VARCHAR(255) NOT NULL,    -- 如: 'England'
    logo_url VARCHAR(1024)
);

CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- 如: 'Manchester United'
    country VARCHAR(255) NOT NULL,
    stadium_name VARCHAR(255),
    logo_url VARCHAR(1024),
    league_id BIGINT,

    CONSTRAINT fk_league
        FOREIGN KEY(league_id)
        REFERENCES leagues(id)
        ON DELETE SET NULL -- 如果联赛被删除，球队的league_id设为NULL
);

CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    known_as VARCHAR(255),            -- 常用名或昵称, 如: 'Bruno Fernandes'
    nationality VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    position VARCHAR(100),            -- 如: 'Attacking Midfield'
    height_cm INT,
    photo_url VARCHAR(1024)
);

-- 为球员姓名创建索引，加快查询速度
CREATE INDEX idx_players_full_name ON players(full_name);


CREATE TABLE player_contracts (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    jersey_number INT,
    market_value_eur BIGINT,          -- 存储来自德转的身价
    contract_start_date DATE,
    contract_end_date DATE,
    is_on_loan BOOLEAN DEFAULT FALSE, -- 是否为租借

    CONSTRAINT fk_player
        FOREIGN KEY(player_id)
        REFERENCES players(id)
        ON DELETE CASCADE, -- 如果球员记录被删除，其所有合同记录也一并删除

    CONSTRAINT fk_team
        FOREIGN KEY(team_id)
        REFERENCES teams(id)
        ON DELETE CASCADE -- 如果球队记录被删除，其所有球员合同也删除
);

-- 创建联合索引，确保一个球员在一个球队只有一条当前记录（可根据业务调整），并加快查询
CREATE INDEX idx_contracts_player_team ON player_contracts(player_id, team_id); 