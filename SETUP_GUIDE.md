# SwiftGoal - 项目设置与用户指南

## 1. 项目概述

欢迎使用 SwiftGoal！本项目是一个基于 Java 和 Python 构建的智能体育新闻聚合与分析平台。它能够抓取、分析、翻译并展示全球体育新闻，并提供数据查询功能。

该项目包含两个核心部分：
*   **Java 后端 (SwiftGoal-App)**: 基于 Spring Boot，负责数据管理、业务逻辑和 Web 界面。
*   **Python AI 服务 (AI-Service)**: 基于 `transformers` 和 `FastAPI`，使用一个本地化的 AI 模型提供翻译功能。

为了完整地运行本项目，您需要同时启动这两个服务。

## 2. 环境要求

在开始之前，请确保您的系统已安装以下软件：

*   **Java**: 版本 17 或更高
*   **Maven**: 版本 3.x 或更高
*   **Python**: 版本 3.8 或更高
*   **Git**: 版本 2.13 或更高 (支持 `--recurse-submodules`)
*   **Git LFS**: 用于下载大体积的模型文件 (`git lfs install` 进行安装)
*   **数据库**: PostgreSQL (推荐) 或 MySQL

## 3. 环境搭建步骤

### 3.1. 获取项目代码 (包含 AI 模型)

打开终端，使用以下命令克隆本仓库。`--recurse-submodules` 参数会自动初始化并下载作为子模块的 AI 模型。

```bash
git clone --recurse-submodules <your-repository-url>
cd SwiftGoal
```

> **如果已经克隆过项目**:
> 如果您之前已经克隆了项目但没有下载子模块，请运行以下命令来获取模型文件：
> ```bash
> git submodule update --init --recursive
> ```

下载完成后，您的项目根目录下应该会有一个 `Qwen2.5-0.5B` 文件夹，其中包含了模型文件。

### 3.2. 数据库设置

项目使用 PostgreSQL 存储数据。

1.  **安装并启动 PostgreSQL**。
2.  **创建一个新的数据库**。例如，我们使用 `swiftgoal` 作为数据库名。
    ```bash
    psql -U postgres
    CREATE DATABASE swiftgoal;
    \q
    ```
    > **注意**: 项目当前使用 `spring.jpa.hibernate.ddl-auto=update` 来自动更新数据库表结构，同时 `spring.flyway.enabled` 被设置为 `false`。这意味着应用在启动时会根据实体类自动调整数据库，无需手动执行 SQL 迁移。

### 3.3. 配置 Java 后端 (SwiftGoal-App)

所有后端相关的配置都在 `src/main/resources/application.properties` 文件中。

1.  **打开 `application.properties` 文件**。
2.  **配置数据库连接**：
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/swiftgoal
    spring.datasource.username=YOUR_POSTGRES_USERNAME
    spring.datasource.password=YOUR_POSTGRES_PASSWORD
    ```
3.  **配置第三方 API 密钥**：
    *   **DeepSeek API Key**: 项目仍使用 DeepSeek API 进行高级分析（如内容摘要、关键词提取）。请在下方填入您的密钥。
        ```properties
        app.deepseek.api-key: YOUR_DEEPSEEK_API_KEY
        ```
    *   **Football-Data.org API Key**: 用于导入足球联赛/球队/球员数据。
        ```properties
        api.football-data.token: YOUR_FOOTBALL_DATA_API_KEY
        ```

### 3.4. 配置 Python AI 服务 (AI-Service)

本项目使用一个本地的 Python 服务来进行文本翻译，模型文件已通过 Git Submodule 提供。

1.  **安装 Python 依赖库**。在项目根目录运行：
    ```bash
    pip install "transformers[torch]" accelerate fastapi uvicorn python-multipart
    ```
2.  **确认模型路径**：`translation_server.py` 脚本已配置为从本地的 `./Qwen2.5-0.5B` 目录加载模型，无需修改。

## 4. 运行项目

您需要**启动两个服务**：Python AI 服务和 Java 后端。请在两个独立的终端中执行以下操作。

### 第 1 步: 启动 Python AI 服务

此服务为 Java 应用提供翻译能力，必须先于 Java 应用启动。

*   在项目根目录的终端中，运行：
    ```bash
    python translation_server.py
    ```
*   如果一切顺利，您将看到类似以下的输出，表示服务已在 `8000` 端口上运行：
    ```
    INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
    ```
*   **保持此终端窗口打开**。

### 第 2 步: 启动 Java 后端

*   打开一个**新的终端窗口**，确保您仍在项目根目录下。
*   您可以使用 Maven 直接运行，或使用我们提供的脚本。

    *   **方式一：使用脚本 (推荐)**
        ```bash
        ./scripts/start-app.sh
        ```

    *   **方式二：使用 Maven 命令**
        ```bash
        mvn spring-boot:run
        ```
*   应用成功启动后，您将在控制台看到 Spring Boot 的启动日志。默认情况下，Web 服务运行在 `8081` 端口。

### 第 3 步: 访问应用

打开您的浏览器，访问 `http://localhost:8081`。您应该能看到 SwiftGoal 的主页。

### 关于数据导入

*   项目包含一个特殊的**数据导入模式**，用于从 `Football-Data.org` API 获取初始数据（联赛、球队、球员等）。
*   如果您的数据库是空的，您可以先运行数据导入脚本：
    ```bash
    ./scripts/start-data-import.sh
    ```
*   该过程会花费一些时间。完成后，再按照上述步骤启动正常应用即可。

## 5. 项目如何工作

### AI 分析流程

当系统需要对一篇新闻文章进行分析时，会执行以下混合流程：

1.  Java 后端的 `AIService` 接收到任务。
2.  它首先将文章**标题**发送到本地运行的 **Python AI 服务** (`http://127.0.0.1:8000/translate/`) 进行翻译。
3.  然后，`AIService` 将**翻译好的标题**和**文章原文**一起，发送到 **DeepSeek API**，请求进行内容摘要、关键词提取和内容分类。
4.  最后，将两部分的结果合并，存入数据库。

这种设计利用了本地模型的低成本和高可用性来处理批量翻译任务，同时利用功能更强大的外部 API 来完成更复杂的分析。

祝您使用愉快！ 