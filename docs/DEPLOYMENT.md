# Deployment & Operations Guide
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Prerequisites
- **Docker Engine:** Version 20.10+ (Docker Desktop on Windows/macOS or Docker Engine on Linux)
- **Docker Compose:** Version 2.0+
- **JDK (for native local execution):** Java 21 LTS
- **Node.js (for native local execution):** Node 18+ or 20+

---

### 2. Quickstart with Docker Compose

The simplest way to deploy the complete platform is via Docker Compose:

```bash
# 1. Clone or navigate to the repository
cd Project-evaluation-system

# 2. Copy the environment template
cp .env.example .env

# 3. Build and launch all services
docker compose up --build
```

#### Services Started:
| Container Name | Service | Internal Port | Host Port | Purpose |
|---|---|---|---|---|
| `projecteval-postgres` | PostgreSQL 16 | 5432 | 5432 / 5433 | Relational database storage |
| `projecteval-backend` | Spring Boot 3 | 8080 | 8080 | REST APIs, execution engines, PDF generator |
| `projecteval-frontend` | Nginx + React | 3000 | 3000 | Production UI with built-in API proxy |

---

### 3. Local Development Deployment

To run services natively without containerization:

#### 1. Start PostgreSQL (e.g. via Docker container)
```bash
docker run -d --name projecteval-pg -p 5433:5432 -e POSTGRES_DB=projecteval -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:16-alpine
```

#### 2. Run the Spring Boot Backend
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
The backend initializes the database schema automatically, seeds development accounts, and listens on `http://localhost:8080`.

#### 3. Run the React Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend starts on `http://localhost:5173` with automatic API proxying.

---

### 4. Default Seed Credentials

> [!IMPORTANT]
> The following credentials are provided for development and testing. Change all passwords in production!

| Role | Email | Password |
|---|---|---|
| **Administrator** | `admin@projecteval.com` | `Admin@123` |
| **Faculty Evaluator** | `evaluator@projecteval.com` | `Evaluator@123` |
| **Student Developer** | `student@projecteval.com` | `Student@123` |
