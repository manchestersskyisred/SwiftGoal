-- Add some demo data for presentation purposes.
-- This script inserts a few well-known players with complete data, including market value.

-- Ensure the league exists (Premier League)
INSERT INTO leagues (id, api_league_id, name_en, name_cn, country_en, country_cn)
VALUES (1, 'PL', 'Premier League', '英超', 'England', '英格兰')
ON CONFLICT (id) DO NOTHING;

-- Ensure teams exist (Manchester City, Arsenal)
INSERT INTO teams (id, api_team_id, name_en, name_cn, short_name_en, short_name_cn, country_en, country_cn, league_id)
VALUES (1, 50, 'Manchester City FC', '曼城', 'Man City', '曼城', 'England', '英格兰', 1),
       (2, 57, 'Arsenal FC', '阿森纳', 'Arsenal', '阿森纳', 'England', '英格兰', 1)
ON CONFLICT (id) DO NOTHING;

-- Insert famous players
INSERT INTO players (id, api_player_id, full_name_en, full_name_cn, date_of_birth, nationality_en, nationality_cn, position_en, position_cn)
VALUES (1, 65, 'Erling Haaland', '埃尔林·哈兰德', '2000-07-21', 'Norway', '挪威', 'Centre-Forward', '中锋'),
       (2, 114, 'Bukayo Saka', '布卡约·萨卡', '2001-09-05', 'England', '英格兰', 'Right Winger', '右边锋'),
       (3, 78, 'Kevin De Bruyne', '凯文·德布劳内', '1991-06-28', 'Belgium', '比利时', 'Attacking Midfield', '攻击型中场'),
       (4, 225, 'Martin Ødegaard', '马丁·厄德高', '1998-12-17', 'Norway', '挪威', 'Attacking Midfield', '攻击型中场')
ON CONFLICT (id) DO NOTHING;

-- Insert player contracts with market values
-- Market values are based on Transfermarkt as of late 2023/early 2024 for demo purposes.
INSERT INTO player_contracts (player_id, team_id, market_value_eur)
VALUES (1, 1, 180000000), -- Haaland -> Man City, €180m
       (2, 2, 140000000), -- Saka -> Arsenal, €140m
       (3, 1, 50000000),  -- De Bruyne -> Man City, €50m
       (4, 2, 110000000)  -- Ødegaard -> Arsenal, €110m
ON CONFLICT (player_id, team_id) DO NOTHING; 