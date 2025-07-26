#!/bin/bash

# SwiftGoal 正常应用启动脚本
# 用于启动Web应用服务器

echo "=== SwiftGoal 应用启动模式 ==="
echo "此模式将："
echo "1. 启动Web应用服务器"
echo "2. 提供游戏和搜索功能"
echo "3. 保持运行状态"
echo ""

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

echo "开始编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi

echo "编译成功，启动应用服务器..."

# 启动应用服务器
mvn spring-boot:run

echo "应用已启动，访问 http://localhost:8080 开始使用" 