@echo off
setlocal enabledelayedexpansion

echo =====================================================================
echo    PROJECT EVALUATION ^& AUTOMATED TESTING SYSTEM (ProjectEval)
echo =====================================================================
echo.

:: Detect if Docker is available and running
docker info >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [INFO] Docker detected and running!
    echo [INFO] Launching full containerized environment via Docker Compose...
    echo.
    docker compose up --build
    if %ERRORLEVEL% neq 0 (
        echo.
        echo [WARN] Docker compose encountered an issue. Falling back to local native mode...
        goto LOCAL_RUN
    )
    goto END
)

:LOCAL_RUN
echo [INFO] Docker is not available or not running. Starting in Local Native Mode...
echo.

:: Check Java
set JAVA_CMD=java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    ) else if exist "%USERPROFILE%\.tools\jdk-21.0.6+7\bin\java.exe" (
        set "JAVA_HOME=%USERPROFILE%\.tools\jdk-21.0.6+7"
        set "PATH=%USERPROFILE%\.tools\jdk-21.0.6+7\bin;%PATH%"
        set "JAVA_CMD=%USERPROFILE%\.tools\jdk-21.0.6+7\bin\java.exe"
    ) else (
        echo [ERROR] Java 21 is required but not found in PATH or JAVA_HOME.
        echo Please install JDK 21 or start Docker Desktop and run this script again.
        pause
        exit /b 1
    )
)

:: Check Node.js
where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Node.js is required for local native mode but not found.
    echo Please install Node.js (v18+) or start Docker Desktop.
    pause
    exit /b 1
)

:: Setup and install frontend dependencies if needed
echo [INFO] Checking frontend dependencies...
if not exist "frontend\node_modules" (
    echo [INFO] Installing frontend packages...
    cd frontend
    call npm install
    cd ..
)

:: Start Backend in separate window with H2 profile (zero DB setup required)
echo [INFO] Starting Backend with in-memory H2 database...
cd backend
if exist "mvnw.cmd" (
    start "ProjectEval Backend (Port 8080)" cmd /c "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2"
) else (
    start "ProjectEval Backend (Port 8080)" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=h2"
)
cd ..

:: Start Frontend Vite Dev Server in separate window
echo [INFO] Starting Frontend Dev Server (Port 5173)...
cd frontend
start "ProjectEval Frontend (Port 5173)" cmd /c "npm run dev"
cd ..

echo.
echo =====================================================================
echo    ProjectEval is starting!
echo    Backend API:  http://localhost:8080
echo    Frontend UI:  http://localhost:5173
echo.
echo    Default Credentials:
echo      - Admin:     admin@projecteval.com     / Admin@123
echo      - Evaluator: evaluator@projecteval.com / Evaluator@123
echo      - Student:   student@projecteval.com   / Student@123
echo.
echo    Opening browser at http://localhost:5173 in 5 seconds...
echo =====================================================================

timeout /t 5 /nobreak >nul
start http://localhost:5173

:END
