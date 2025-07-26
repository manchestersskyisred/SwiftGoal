# SwiftGoal 数据导入指南

## 概述

SwiftGoal应用提供了两种启动模式：
1. **数据导入模式**：专门用于导入足球数据并执行中文名翻译
2. **正常应用模式**：启动Web应用服务器，提供游戏和搜索功能

## 启动模式

### 1. 数据导入模式

用于首次设置或更新数据时使用。

```bash
# 使用脚本启动数据导入模式
./scripts/start-data-import.sh

# 或手动启动
export APP_MODE=data-import
mvn spring-boot:run -Dspring-boot.run.profiles=data-import
```

**数据导入模式会执行：**
- 从 Football-Data.org API 导入五大联赛数据
- 导入所有球队和球员信息
- 执行中文名翻译
- 完成后自动退出

### 2. 正常应用模式

用于日常使用，启动Web应用服务器。

```bash
# 使用脚本启动正常应用模式
./scripts/start-app.sh

# 或手动启动
mvn spring-boot:run
```

**正常应用模式会：**
- 启动Web应用服务器（端口8080）
- 提供游戏功能
- 提供搜索功能
- 保持运行状态

## 使用流程

### 首次设置

1. **准备数据库**
   ```bash
   # 确保PostgreSQL数据库已启动
   # 创建数据库
   createdb swiftgoal
   ```

2. **配置API密钥**
   ```bash
   # 设置Football-Data.org API密钥
   export FOOTBALL_DATA_TOKEN=your-api-token-here
   ```

3. **执行数据导入**
   ```bash
   ./scripts/start-data-import.sh
   ```

4. **启动应用**
   ```bash
   ./scripts/start-app.sh
   ```

### 日常使用

```bash
# 直接启动应用
./scripts/start-app.sh
```

### 更新数据

```bash
# 重新导入数据
./scripts/start-data-import.sh
```

## 配置说明

### 环境变量

- `FOOTBALL_DATA_TOKEN`: Football-Data.org API密钥
- `APP_MODE`: 应用模式（data-import 或 正常模式）

### 数据库配置

在 `application-data-import.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/swiftgoal
    username: postgres
    password: postgres
```

## 数据导入详情

### 导入的联赛

- Premier League (英格兰足球超级联赛)
- La Liga (西班牙足球甲级联赛)
- Bundesliga (德国足球甲级联赛)
- Serie A (意大利足球甲级联赛)
- Ligue 1 (法国足球甲级联赛)

### 导入的数据

1. **联赛信息**
   - 联赛名称（中英文）
   - 所属国家（中英文）
   - 联赛标识

2. **球队信息**
   - 球队名称（中英文）
   - 球队简称（中英文）
   - 所属国家（中英文）
   - 主场球场（中英文）
   - 球队标识

3. **球员信息**
   - 球员姓名（中英文）
   - 国籍（中英文）
   - 位置（中英文）
   - 出生日期
   - 身高
   - 照片URL

4. **球员合同信息**
   - 球衣号码
   - 市场价值
   - 合同期限
   - 租借状态

### 中文名翻译

数据导入完成后会自动执行中文名翻译，包括：

- 知名球员的中文译名
- 知名球队的中文译名
- 五大联赛的中文译名
- 常见国籍的中文译名
- 常见位置的中文译名

## 故障排除

### 常见问题

1. **编译失败**
   ```bash
   # 清理并重新编译
   mvn clean compile
   ```

2. **数据库连接失败**
   - 检查PostgreSQL是否启动
   - 检查数据库配置
   - 检查网络连接

3. **API调用失败**
   - 检查API密钥是否正确
   - 检查网络连接
   - 检查API配额是否用完

4. **中文名翻译失败**
   - 检查数据库连接
   - 检查实体类配置
   - 查看日志信息

### 日志查看

```bash
# 查看详细日志
tail -f logs/application.log
```

## 性能优化

### 数据导入优化

1. **API调用限制**
   - 免费版API限制为10次/分钟
   - 导入过程会自动控制调用频率

2. **数据库优化**
   - 使用索引提高查询性能
   - 批量操作减少数据库交互

3. **内存优化**
   - 分批处理大量数据
   - 及时释放不需要的对象

### 应用运行优化

1. **缓存策略**
   - 缓存翻译结果
   - 缓存常用查询

2. **连接池配置**
   - 优化数据库连接池
   - 优化HTTP连接池

## 监控和维护

### 数据监控

```bash
# 检查数据导入状态
curl http://localhost:8080/api/translation/translate-all
```

### 定期维护

1. **数据更新**
   - 定期执行数据导入
   - 更新中文名翻译

2. **数据库维护**
   - 定期备份数据
   - 清理过期数据

3. **日志清理**
   - 定期清理日志文件
   - 监控磁盘空间

## 扩展功能

### 自定义翻译

可以在 `ChineseNameTranslationService` 中添加自定义翻译：

```java
// 添加新球员翻译
PLAYER_NAME_TRANSLATIONS.put("New Player", "新球员");

// 添加新球队翻译
TEAM_NAME_TRANSLATIONS.put("New Team", "新球队");
```

### 集成其他数据源

可以扩展 `DataImportService` 来集成其他数据源：

```java
// 添加新的数据源
public void importFromOtherSource() {
    // 实现新的数据导入逻辑
}
```

## 总结

通过分离数据导入和正常应用启动，SwiftGoal提供了更清晰的使用模式：

- **数据导入模式**：专门用于数据管理和维护
- **正常应用模式**：提供完整的Web应用功能

这种设计使得数据管理更加可控，同时保持了应用的简洁性。 