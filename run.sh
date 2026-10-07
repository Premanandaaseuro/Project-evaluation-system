#!/usr/bin/env bash
set -e

echo "====================================================================="
echo "   PROJECT EVALUATION & AUTOMATED TESTING SYSTEM (ProjectEval)"
echo "====================================================================="
echo ""

# 1. Check if Docker is running
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    echo "[INFO] Docker detected and running! Starting full stack with Docker Compose..."
    echo ""
    docker compose up --build
    exit 0
fi

echo "[INFO] Docker is not running or not installed. Switching to Local Native Mode..."
echo ""

# 2. Check Java
if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] Java 21 is required. Please install OpenJDK 21 or start Docker Desktop."
    exit 1
fi

# 3. Check Node
if ! command -v node >/dev/null 2>&1; then
    echo "[ERROR] Node.js is required. Please install Node.js (v18+) or start Docker Desktop."
    exit 1
fi

# 4. Frontend npm install if needed
if [ ! -d "frontend/node_modules" ]; then
    echo "[INFO] Installing frontend dependencies..."
    (cd frontend && npm install)
fi

# 5. Start Backend with H2 in-memory DB
echo "[INFO] Starting Backend with in-memory H2 database on port 8080..."
if [ -f "backend/mvnw" ]; then
    chmod +x backend/mvnw
    (cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2) &
else
    (cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=h2) &
fi
BACKEND_PID=$!

# 6. Start Frontend Vite Dev Server
echo "[INFO] Starting Frontend Dev Server on port 5173..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT

echo ""
echo "====================================================================="
echo "   ProjectEval is running!"
echo "   Backend API:  http://localhost:8080"
echo "   Frontend UI:  http://localhost:5173"
echo "   Default Credentials:"
echo "     - Admin:     admin@projecteval.com     / Admin@123"
echo "     - Evaluator: evaluator@projecteval.com / Evaluator@123"
echo "     - Student:   student@projecteval.com   / Student@123"
echo "   Press Ctrl+C to stop all servers."
echo "====================================================================="

# Attempt to open browser
if command -v xdg-open >/dev/null 2>&1; then
    sleep 4 && xdg-open "http://localhost:5173" &
elif command -v open >/dev/null 2>&1; then
    sleep 4 && open "http://localhost:5173" &
fi

wait
