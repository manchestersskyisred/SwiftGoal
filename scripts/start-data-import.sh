#!/bin/bash

# SwiftGoal 数据导入启动脚本
# 用于导入足球数据并执行中文名翻译

echo "=== SwiftGoal 数据导入模式 ==="
echo "此模式将："
echo "1. 从 Football-Data.org API 导入联赛、球队和球员数据"
echo "2. 执行中文名翻译"
echo "3. 完成后自动退出"
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

echo "编译成功，开始数据导入..."

# 设置数据导入模式并启动应用
export APP_MODE=data-import
mvn spring-boot:run -Dspring-boot.run.profiles=data-import

echo "数据导入完成！" 