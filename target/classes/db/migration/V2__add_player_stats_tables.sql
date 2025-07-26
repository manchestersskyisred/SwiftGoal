CREATE TABLE player_season_stats (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    league_id BIGINT NOT NULL,
    season INT NOT NULL,
    minutes_played INT,
    appearances INT,
    lineups INT,
    goals INT,
    assists INT,
    -- Add other aggregated stats as needed from the API
    CONSTRAINT fk_player_season FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_season FOREIGN KEY(team_id) REFERENCES teams(id),
    CONSTRAINT fk_league_season FOREIGN KEY(league_id) REFERENCES leagues(id),
    UNIQUE(player_id, team_id, league_id, season)
);

CREATE TABLE player_match_stats (
    id BIGSERIAL PRIMARY KEY,
    -- Foreign Keys
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    opponent_id BIGINT NOT NULL,
    fixture_api_id BIGINT UNIQUE,
    
    -- Match Info
    competition_name VARCHAR(255) NOT NULL,
    match_date DATE NOT NULL,
    venue_name VARCHAR(255),
    is_home_match BOOLEAN,
    
    -- Player Stats
    minutes_played INT,
    player_rating REAL,
    is_starter BOOLEAN,
    is_substitute BOOLEAN,
    
    -- Attacking Stats
    goals INT DEFAULT 0,
    assists INT DEFAULT 0,
    shots_total INT,
    shots_on_goal INT,
    passes_total INT,
    passes_key INT,
    dribbles_attempts INT,
    dribbles_success INT,
    
    -- Defensive Stats
    tackles_total INT,
    interceptions INT,
    duels_total INT,
    duels_won INT,
    
    -- Discipline
    yellow_card BOOLEAN DEFAULT FALSE,
    red_card BOOLEAN DEFAULT FALSE,

    -- Constraints
    CONSTRAINT fk_player_match FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_match FOREIGN KEY(team_id) REFERENCES teams(id),
    CONSTRAINT fk_opponent_match FOREIGN KEY(opponent_id) REFERENCES teams(id)
);

CREATE INDEX idx_match_stats_player_season ON player_match_stats(player_id, match_date DESC); 