@echo off
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not running or not installed.
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Starting GDS SWE Challenge App via Docker...
docker-compose up --build
pause
