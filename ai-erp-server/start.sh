#!/bin/bash
# start.sh - AI ERP 启动脚本

cd /root/.openclaw/workspace/project/ai-erp-server

# 创建数据目录
mkdir -p ./data
mkdir -p ./logs

# 启动应用
nohup java -Xms256m -Xmx512m -jar target/ai-erp-server-1.0.0.jar > logs/app.log 2>&1 &

echo $! > app.pid

echo "========================================"
echo "  AI Native ERP Started!"
echo "  PID: $(cat app.pid)"
echo "  URL: http://localhost:8080"
echo "  Admin: admin / admin123"
echo "========================================"
