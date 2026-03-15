#!/bin/bash
# AI-ERP Server 停止脚本

echo "Stopping AI-ERP Server..."

# 查找并停止Java进程
PID=$(ps aux | grep 'ai-erp-server' | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
    kill $PID
    echo "AI-ERP Server stopped (PID: $PID)"
else
    echo "AI-ERP Server is not running"
fi
