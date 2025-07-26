-- 增强中文名支持 - 为leagues、teams和players表添加完整的中文译名字段

-- 1. 为leagues表添加中文名字段
ALTER TABLE leagues ADD COLUMN name_cn VARCHAR(255) UNIQUE;
ALTER TABLE leagues ADD COLUMN country_cn VARCHAR(255);

-- 将现有的name和country字段重命名为英文版本
ALTER TABLE leagues RENAME COLUMN name TO name_en;
ALTER TABLE leagues RENAME COLUMN country TO country_en;

-- 为leagues表添加索引
CREATE INDEX idx_leagues_name_en ON leagues(name_en);
CREATE INDEX idx_leagues_name_cn ON leagues(name_cn);

-- 2. 为teams表添加中文名字段
ALTER TABLE teams ADD COLUMN name_cn VARCHAR(255) UNIQUE;
ALTER TABLE teams ADD COLUMN short_name_en VARCHAR(100);
ALTER TABLE teams ADD COLUMN short_name_cn VARCHAR(100);
ALTER TABLE teams ADD COLUMN country_cn VARCHAR(255);
ALTER TABLE teams ADD COLUMN stadium_name_cn VARCHAR(255);

-- 将现有的name、country、stadium_name字段重命名为英文版本
ALTER TABLE teams RENAME COLUMN name TO name_en;
ALTER TABLE teams RENAME COLUMN country TO country_en;
ALTER TABLE teams RENAME COLUMN stadium_name TO stadium_name_en;

-- 为teams表添加索引
CREATE INDEX idx_teams_name_en ON teams(name_en);
CREATE INDEX idx_teams_name_cn ON teams(name_cn);
CREATE INDEX idx_teams_short_name_en ON teams(short_name_en);
CREATE INDEX idx_teams_short_name_cn ON teams(short_name_cn);

-- 3. 为players表添加更多中文名字段
ALTER TABLE players ADD COLUMN nationality_cn VARCHAR(255);
ALTER TABLE players ADD COLUMN position_cn VARCHAR(100);

-- 将现有的nationality和position字段重命名为英文版本
ALTER TABLE players RENAME COLUMN nationality TO nationality_en;
ALTER TABLE players RENAME COLUMN position TO position_en;

-- 为players表添加新的索引
CREATE INDEX idx_players_nationality_en ON players(nationality_en);
CREATE INDEX idx_players_nationality_cn ON players(nationality_cn);
CREATE INDEX idx_players_position_en ON players(position_en);
CREATE INDEX idx_players_position_cn ON players(position_cn);

-- 4. 添加一些知名联赛的中文名示例
UPDATE leagues SET 
    name_cn = '英格兰足球超级联赛',
    country_cn = '英格兰'
WHERE name_en = 'Premier League';

UPDATE leagues SET 
    name_cn = '西班牙足球甲级联赛',
    country_cn = '西班牙'
WHERE name_en = 'La Liga';

UPDATE leagues SET 
    name_cn = '德国足球甲级联赛',
    country_cn = '德国'
WHERE name_en = 'Bundesliga';

UPDATE leagues SET 
    name_cn = '意大利足球甲级联赛',
    country_cn = '意大利'
WHERE name_en = 'Serie A';

UPDATE leagues SET 
    name_cn = '法国足球甲级联赛',
    country_cn = '法国'
WHERE name_en = 'Ligue 1';

-- 5. 添加一些知名球队的中文名示例
UPDATE teams SET 
    name_cn = '曼彻斯特联',
    short_name_en = 'Man United',
    short_name_cn = '曼联',
    country_cn = '英格兰',
    stadium_name_cn = '老特拉福德球场'
WHERE name_en = 'Manchester United';

UPDATE teams SET 
    name_cn = '曼彻斯特城',
    short_name_en = 'Man City',
    short_name_cn = '曼城',
    country_cn = '英格兰',
    stadium_name_cn = '伊蒂哈德球场'
WHERE name_en = 'Manchester City';

UPDATE teams SET 
    name_cn = '阿森纳',
    short_name_en = 'Arsenal',
    short_name_cn = '阿森纳',
    country_cn = '英格兰',
    stadium_name_cn = '酋长球场'
WHERE name_en = 'Arsenal';

UPDATE teams SET 
    name_cn = '利物浦',
    short_name_en = 'Liverpool',
    short_name_cn = '利物浦',
    country_cn = '英格兰',
    stadium_name_cn = '安菲尔德球场'
WHERE name_en = 'Liverpool';

UPDATE teams SET 
    name_cn = '切尔西',
    short_name_en = 'Chelsea',
    short_name_cn = '切尔西',
    country_cn = '英格兰',
    stadium_name_cn = '斯坦福桥球场'
WHERE name_en = 'Chelsea';

UPDATE teams SET 
    name_cn = '托特纳姆热刺',
    short_name_en = 'Tottenham',
    short_name_cn = '热刺',
    country_cn = '英格兰',
    stadium_name_cn = '托特纳姆热刺球场'
WHERE name_en = 'Tottenham Hotspur';

UPDATE teams SET 
    name_cn = '皇家马德里',
    short_name_en = 'Real Madrid',
    short_name_cn = '皇马',
    country_cn = '西班牙',
    stadium_name_cn = '伯纳乌球场'
WHERE name_en = 'Real Madrid';

UPDATE teams SET 
    name_cn = '巴塞罗那',
    short_name_en = 'Barcelona',
    short_name_cn = '巴萨',
    country_cn = '西班牙',
    stadium_name_cn = '诺坎普球场'
WHERE name_en = 'Barcelona';

UPDATE teams SET 
    name_cn = '拜仁慕尼黑',
    short_name_en = 'Bayern Munich',
    short_name_cn = '拜仁',
    country_cn = '德国',
    stadium_name_cn = '安联球场'
WHERE name_en = 'Bayern Munich';

UPDATE teams SET 
    name_cn = '多特蒙德',
    short_name_en = 'Borussia Dortmund',
    short_name_cn = '多特',
    country_cn = '德国',
    stadium_name_cn = '西格纳伊度纳公园球场'
WHERE name_en = 'Borussia Dortmund';

-- 6. 添加一些知名球员的中文名示例
UPDATE players SET 
    nationality_cn = '阿根廷',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Lionel Messi%';

UPDATE players SET 
    nationality_cn = '葡萄牙',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Cristiano Ronaldo%';

UPDATE players SET 
    nationality_cn = '巴西',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Neymar%';

UPDATE players SET 
    nationality_cn = '法国',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Kylian Mbappé%';

UPDATE players SET 
    nationality_cn = '挪威',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Erling Haaland%';

UPDATE players SET 
    nationality_cn = '比利时',
    position_cn = '中场'
WHERE full_name_en LIKE '%Kevin De Bruyne%';

UPDATE players SET 
    nationality_cn = '克罗地亚',
    position_cn = '中场'
WHERE full_name_en LIKE '%Luka Modrić%';

UPDATE players SET 
    nationality_cn = '法国',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Karim Benzema%';

UPDATE players SET 
    nationality_cn = '波兰',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Robert Lewandowski%';

UPDATE players SET 
    nationality_cn = '埃及',
    position_cn = '前锋'
WHERE full_name_en LIKE '%Mohamed Salah%';

-- 添加表注释
COMMENT ON TABLE leagues IS '存储五大联赛等核心赛事的基本信息，包含中英文名称';
COMMENT ON TABLE teams IS '存储球队的静态信息，包含中英文名称';
COMMENT ON TABLE players IS '存储球员不随时间改变的核心静态信息，包含中英文名称'; 