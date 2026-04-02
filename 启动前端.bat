@echo off
chcp 65001 >nul
title 图书馆前端开发服务器

cd /d "%~dp0library-web"

echo ================================
echo   图书馆RFID数据安全管理系统
echo   前端开发服务器启动中...
echo ================================
echo.

npm run dev

pause
