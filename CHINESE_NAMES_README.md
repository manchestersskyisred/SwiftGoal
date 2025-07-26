# 中文名支持功能说明

## 概述

SwiftGoal应用现已全面支持中英文双语显示，为国内用户提供更好的使用体验。本功能为球员、球队和联赛提供了完整的中文译名支持。

## 数据库结构升级

### 1. Leagues（联赛表）

**新增字段：**
- `name_cn` VARCHAR(255) UNIQUE - 中文名称
- `country_cn` VARCHAR(255) - 中文国家名

**重命名字段：**
- `name` → `name_en` - 英文名称
- `country` → `country_en` - 英文国家名

**示例：**
- 英文：Premier League (England)
- 中文：英格兰足球超级联赛 (英格兰)

### 2. Teams（球队表）

**新增字段：**
- `name_cn` VARCHAR(255) UNIQUE - 中文全名
- `short_name_en` VARCHAR(100) - 英文简称
- `short_name_cn` VARCHAR(100) - 中文简称
- `country_cn` VARCHAR(255) - 中文国家名
- `stadium_name_cn` VARCHAR(255) - 中文球场名

**重命名字段：**
- `name` → `name_en` - 英文全名
- `country` → `country_en` - 英文国家名
- `stadium_name` → `stadium_name_en` - 英文球场名

**示例：**
- 英文：Manchester United (England) - Old Trafford
- 中文：曼彻斯特联 (英格兰) - 老特拉福德球场

### 3. Players（球员表）

**新增字段：**
- `nationality_cn` VARCHAR(255) - 中文国籍
- `position_cn` VARCHAR(100) - 中文位置

**重命名字段：**
- `nationality` → `nationality_en` - 英文国籍
- `position` → `position_en` - 英文位置

**示例：**
- 英文：Lionel Messi (Argentina) - Forward
- 中文：梅西 (阿根廷) - 前锋

## 功能特性

### 1. 多语言搜索

支持中英文混合搜索，用户可以使用中文或英文进行搜索：

- 球员搜索：支持中文名、英文名、昵称搜索
- 球队搜索：支持中文名、英文名、简称搜索
- 联赛搜索：支持中文名、英文名搜索

### 2. 智能显示

系统会根据是否有中文名自动选择显示方式：

- 有中文名：显示"中文名 (英文名)"格式
- 无中文名：仅显示英文名

### 3. 自动翻译服务

提供了预定义的翻译映射，包含：

- 知名球员的中文译名
- 知名球队的中文译名
- 五大联赛的中文译名
- 常见国籍的中文译名
- 常见位置的中文译名

## API接口

### 翻译管理接口

```
POST /api/translation/translate-all      # 执行所有翻译
POST /api/translation/translate-players  # 翻译球员中文名
POST /api/translation/translate-teams    # 翻译球队中文名
POST /api/translation/translate-leagues  # 翻译联赛中文名
```

### 查询接口

```
GET /api/translation/player/{englishName}  # 获取球员中文名
GET /api/translation/team/{englishName}    # 获取球队中文名
GET /api/translation/league/{englishName}  # 获取联赛中文名
```

## 使用方法

### 1. 数据库迁移

运行数据库迁移脚本：

```sql
-- 执行V6__enhance_chinese_names.sql迁移
```

### 2. 启动翻译服务

调用API接口执行翻译：

```bash
curl -X POST http://localhost:8080/api/translation/translate-all
```

### 3. 搜索示例

```java
// 球员搜索 - 支持中英文
List<Player> players = playerRepository.searchPlayersByName("梅西");
List<Player> players = playerRepository.searchPlayersByName("Messi");

// 球队搜索 - 支持中英文
List<Team> teams = teamRepository.searchTeamsByName("曼联");
List<Team> teams = teamRepository.searchTeamsByName("Manchester United");
```

## 扩展翻译

### 1. 添加新的翻译

在`ChineseNameTranslationService`中添加新的翻译映射：

```java
// 添加新球员翻译
PLAYER_NAME_TRANSLATIONS.put("New Player Name", "新球员中文名");

// 添加新球队翻译
TEAM_NAME_TRANSLATIONS.put("New Team Name", "新球队中文名");
```

### 2. 外部翻译API集成

可以集成第三方翻译API（如百度翻译、谷歌翻译）来扩展翻译能力：

```java
// 示例：集成AI翻译
public String translateWithAI(String englishName) {
    // 调用AI翻译API
    return aiTranslationService.translate(englishName, "en", "zh");
}
```

### 3. 新闻内容挖掘

利用现有的新闻爬虫功能，从中文新闻中提取球员和球队的中文译名：

```java
// 从新闻内容中提取中文名
public void extractNamesFromNews() {
    // 分析新闻内容，提取中文名
    // 匹配到对应的英文名
    // 更新数据库
}
```

## 性能优化

### 1. 索引优化

为所有搜索字段创建了索引：

```sql
CREATE INDEX idx_players_full_name_en ON players(full_name_en);
CREATE INDEX idx_players_full_name_cn ON players(full_name_cn);
CREATE INDEX idx_teams_name_en ON teams(name_en);
CREATE INDEX idx_teams_name_cn ON teams(name_cn);
```

### 2. 缓存策略

建议为翻译结果添加缓存：

```java
@Cacheable("playerTranslations")
public String getPlayerChineseName(String englishName) {
    return PLAYER_NAME_TRANSLATIONS.get(englishName);
}
```

## 向后兼容性

为了保持向后兼容性，所有实体类都保留了原有的getter方法：

```java
// 向后兼容的方法
public String getName() {
    return nameEn;  // 返回英文名
}

public String getCountry() {
    return countryEn;  // 返回英文国家名
}
```

## 未来计划

1. **AI翻译集成**：集成DeepSeek AI进行自动翻译
2. **用户反馈系统**：允许用户提交翻译建议
3. **多语言支持**：扩展到其他语言（如日语、韩语）
4. **实时翻译**：为新增的球员和球队提供实时翻译服务

## 注意事项

1. 中文名字段设置了UNIQUE约束，确保数据一致性
2. 翻译服务是幂等的，可以重复执行
3. 建议定期备份翻译映射数据
4. 在生产环境中，建议使用事务来确保数据一致性 