#!/bin/bash
# stop.sh - AI ERP 停止脚本

cd /root/.openclaw/workspace/project/ai-erp-server

if [ -f "app.pid" ]; then
    PID=$(cat app.pid)
    kill $PID 2>/dev/null
    rm app.pid
    echo "Application stopped. PID: $PID"
else
    echo "PID file not found. Trying to kill by port..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    echo "Application stopped."
fi
