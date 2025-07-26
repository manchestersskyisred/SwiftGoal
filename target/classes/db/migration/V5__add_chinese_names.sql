-- 添加中文名字段
ALTER TABLE players ADD COLUMN full_name_cn VARCHAR(255);
ALTER TABLE players ADD COLUMN full_name_en VARCHAR(255);

-- 将现有的full_name数据迁移到full_name_en
UPDATE players SET full_name_en = full_name WHERE full_name_en IS NULL;

-- 为一些知名球员添加中文名（示例数据）
UPDATE players SET full_name_cn = '梅西' WHERE full_name_en LIKE '%Lionel Messi%';
UPDATE players SET full_name_cn = '克里斯蒂亚诺·罗纳尔多' WHERE full_name_en LIKE '%Cristiano Ronaldo%';
UPDATE players SET full_name_cn = '内马尔' WHERE full_name_en LIKE '%Neymar%';
UPDATE players SET full_name_cn = '姆巴佩' WHERE full_name_en LIKE '%Kylian Mbappé%';
UPDATE players SET full_name_cn = '哈兰德' WHERE full_name_en LIKE '%Erling Haaland%';
UPDATE players SET full_name_cn = '德布劳内' WHERE full_name_en LIKE '%Kevin De Bruyne%';
UPDATE players SET full_name_cn = '莫德里奇' WHERE full_name_en LIKE '%Luka Modrić%';
UPDATE players SET full_name_cn = '本泽马' WHERE full_name_en LIKE '%Karim Benzema%';
UPDATE players SET full_name_cn = '莱万多夫斯基' WHERE full_name_en LIKE '%Robert Lewandowski%';
UPDATE players SET full_name_cn = '萨拉赫' WHERE full_name_en LIKE '%Mohamed Salah%';

-- 创建索引
CREATE INDEX idx_players_full_name_en ON players(full_name_en);
CREATE INDEX idx_players_full_name_cn ON players(full_name_cn);
CREATE INDEX idx_players_known_as ON players(known_as); 