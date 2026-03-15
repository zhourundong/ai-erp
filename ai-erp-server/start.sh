#!/bin/bash
# AI-ERP Server 启动脚本

echo "Starting AI-ERP Server..."

# 检查Java版本
java -version

# 启动应用 (开发模式)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo "AI-ERP Server started on http://localhost:8080"
