# =====================================================================
#    PROJECT EVALUATION & AUTOMATED TESTING SYSTEM (ProjectEval)
#    PowerShell 1-Command Launcher
# =====================================================================

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "   PROJECT EVALUATION & AUTOMATED TESTING SYSTEM (ProjectEval)" -ForegroundColor Yellow
Write-Host "=====================================================================" -ForegroundColor Cyan

# 1. Check if Docker is running
$dockerRunning = $false
try {
    $null = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        $dockerRunning = $true
    }
} catch {}

if ($dockerRunning) {
    Write-Host "[INFO] Docker detected and running! Starting containerized stack..." -ForegroundColor Green
    docker compose up --build
    exit
}

Write-Host "[INFO] Docker is not running. Starting in Local Native Mode..." -ForegroundColor Yellow

# 2. Check Java
$javaAvailable = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaAvailable) {
    $customJdk = "$env:USERPROFILE\.tools\jdk-21.0.6+7\bin"
    if (Test-Path "$customJdk\java.exe") {
        $env:JAVA_HOME = "$env:USERPROFILE\.tools\jdk-21.0.6+7"
        $env:PATH = "$customJdk;$env:PATH"
        Write-Host "[INFO] Using JDK 21 from $env:JAVA_HOME" -ForegroundColor Cyan
    } else {
        Write-Error "Java 21 is required but not found in PATH or JAVA_HOME. Please install Java 21 or start Docker Desktop."
        exit 1
    }
}

# 3. Check Node
$nodeAvailable = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeAvailable) {
    Write-Error "Node.js (v18+) is required for frontend dev server. Please install Node.js."
    exit 1
}

# 4. Frontend npm install if needed
if (-not (Test-Path "frontend\node_modules")) {
    Write-Host "[INFO] Installing frontend dependencies..." -ForegroundColor Cyan
    Push-Location frontend
    npm install
    Pop-Location
}

# 5. Start Backend
Write-Host "[INFO] Launching backend server with H2 in-memory DB (Port 8080)..." -ForegroundColor Green
$backendCmd = if (Test-Path "backend\mvnw.cmd") { ".\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2" } else { "mvn spring-boot:run -Dspring-boot.run.profiles=h2" }
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; $backendCmd"

# 6. Start Frontend
Write-Host "[INFO] Launching frontend Vite server (Port 5173)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd frontend; npm run dev"

Write-Host ""
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "   ProjectEval is starting!" -ForegroundColor Green
Write-Host "   Backend API:  http://localhost:8080" -ForegroundColor White
Write-Host "   Frontend UI:  http://localhost:5173" -ForegroundColor White
Write-Host "   Default Credentials:" -ForegroundColor Yellow
Write-Host "     - Admin:     admin@projecteval.com     / Admin@123" -ForegroundColor Gray
Write-Host "     - Evaluator: evaluator@projecteval.com / Evaluator@123" -ForegroundColor Gray
Write-Host "     - Student:   student@projecteval.com   / Student@123" -ForegroundColor Gray
Write-Host "=====================================================================" -ForegroundColor Cyan

Start-Sleep -Seconds 5
Start-Process "http://localhost:5173"
