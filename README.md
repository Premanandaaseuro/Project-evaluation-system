# ProjectEval
### Automated Project Evaluation & Testing Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue.svg)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-3.4-38bdf8.svg)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ed.svg)](https://www.docker.com/)

**ProjectEval** is an enterprise-grade automated project evaluation, testing, code quality, security analysis, and grading platform designed for universities, colleges, technical assessment portals, and engineering bootcamps.

The platform executes a **two-level evaluation workflow**:
1. **Level 1 — Automated Evaluation (85 Marks):** Automatically builds, probes, analyzes, and tests submitted source code inside an isolated sandbox environment.
2. **Level 2 — Manual Faculty Evaluation (15 Marks):** Faculty evaluators review code originality, engineering rigor, and presentation, awarding marks and qualitative feedback.
3. **Combined Institutional Scorecard (100 Marks):** Computes total score and official letter grade (`A+`, `A`, `B`, `C`, `D`, `Needs Improvement`), rendering downloadable vector **PDF Evaluation Reports**.

---

## 🌟 Key Features

### 1. Dual-Tier Evaluation Framework
- **Automated Verification (85 Marks):**
  - **Project Build & Startup (10 Marks):** Dependency resolution, compilation, entrypoint startup, and health probe check.
  - **API Functionality (20 Marks):** Endpoint discovery, HTTP methods (GET, POST, PUT, DELETE), JSON payload validation, status codes.
  - **UI Functionality (15 Marks):** Page load, route guards, form validations, data tables, and console error cleanliness.
  - **Database & Integration (15 Marks):** Connection pooling, entity schema mapping, constraints, and ACID transactions.
  - **Requirements Implementation (15 Marks):** Parses student's declared requirements and computes coverage percentage.
  - **Security & Vulnerabilities (5 Marks):** Scans for hardcoded secrets, exposed credentials, and cryptographic password hashing.
  - **Code Quality & Modularity (3 Marks):** Evaluates layered architecture, exception handling, and naming conventions.
  - **Documentation & README (2 Marks):** Evaluates repository documentation completeness and local setup instructions.
- **Manual Faculty Scoring (15 Marks):**
  - Innovation & Novelty (3 Marks)
  - Technical Rigor (4 Marks)
  - Documentation Quality (3 Marks)
  - Presentation & Demo Flow (2 Marks)
  - Final Outcome & Polish (3 Marks)

### 2. Multi-Role Portals
- **Administrator Portal:**
  - High-level KPI metric cards and analytics dashboards.
  - Visual charts: Project status breakdown, institutional grade distribution, department averages.
  - Student and Evaluator directories with active/disable controls.
  - Faculty evaluator assignment controls.
  - Live rubric criteria customization and grade range management.
  - Immutable activity and security audit trail.
- **Student Developer Portal:**
  - 6-Stage project lifecycle tracker (`DRAFT` → `SUBMITTED` → `ASSIGNED` → `EVALUATING` → `EVALUATED` → `PUBLISHED`).
  - GitHub repository submission and ZIP archive upload with ZIP Slip traversal protection.
  - Requirements declaration checklist editor.
  - Granular test-by-test inspection view with expected vs actual results and execution durations.
  - Terminal-style live build log viewer.
  - Downloadable official vector PDF scorecards.
- **Faculty Evaluator Portal:**
  - Assigned projects review queue.
  - Repository inspection and automated test evidence explorer.
  - Interactive manual scoring sliders for the 5 qualitative criteria.
  - Qualitative feedback and observations editor with instant grade calculation.

### 3. Isolated Sandbox & Zero-Trust Architecture
- Ephemeral workspace provisioning per evaluation.
- CPU quotas (1.0 vCPU), memory limits (512MB RAM), and strict 120-second execution timeouts.
- Host filesystem isolation and recursive workspace teardown.
- ZIP archive traversal sanitization preventing malicious path escaping.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 18, TypeScript, Vite, React Router v6, Axios, Tailwind CSS, Lucide React |
| **Backend** | Java 21 LTS, Spring Boot 3.2.5, Spring Security, JWT (JJWT 0.12.5), Spring Data JPA, Hibernate, Maven |
| **Database** | PostgreSQL 16 (production/docker) & H2 in-memory (offline test mode) |
| **PDF Engine** | OpenPDF (LibrePDF 2.0.3) server-side vector PDF generation |
| **Sandboxing** | Docker, Docker Compose, ephemeral workspaces |
| **Testing** | JUnit 5, AssertJ, Spring Security Test, Playwright integration |

---

---

## ⚡ 1-Command Startup (Zero Errors Guarantee)

You can clone and launch the entire application with **a single command** on any platform:

```bash
# 1. Clone repository
git clone https://github.com/Premanandaaseuro/Project-evaluation-system.git
cd Project-evaluation-system
```

### Choose your single command:

#### Option A: Windows (CMD or Double-Click)
```cmd
run.bat
```
*(Or in PowerShell: `.\run.ps1`)*

#### Option B: Linux or macOS
```bash
chmod +x run.sh && ./run.sh
```

#### Option C: Docker Compose (Universal)
```bash
docker compose up --build
```

#### Option D: Node / npm
```bash
npm start
```

### 🧠 How the 1-Command Launcher Works
The launcher automatically adapts to your local machine:
1. **If Docker is running:** It launches the full containerized stack (PostgreSQL 16 + Spring Boot 21 + React 18 / Nginx proxy) and serves the application at **`http://localhost:3000`** (backend on `http://localhost:8080`).
2. **If Docker is NOT running:** It automatically switches to **Local Native Mode**:
   - Uses embedded Maven Wrapper (`mvnw` / `mvnw.cmd` - no Maven installation needed!).
   - Starts Spring Boot with in-memory **H2 database** (`--spring.profiles.active=h2` - no database installation needed!).
   - Automatically installs frontend npm packages if missing and starts Vite.
   - Automatically opens your default browser at **`http://localhost:5173`**.
   - Database is auto-migrated and pre-seeded with Admin, Evaluator, and Student test accounts!

---

## 💻 Local Development Setup (Native)

### Prerequisites
- Java 21 LTS (`java -version`)
- Apache Maven 3.9+ (`mvn -version`)
- Node.js 18+ or 20+ (`node -version`)
- Docker (for PostgreSQL container)

### Step 1: Start PostgreSQL Database
```bash
docker run -d --name projecteval-pg -p 5433:5432 -e POSTGRES_DB=projecteval -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:16-alpine
```

### Step 2: Run the Spring Boot Backend
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
*The backend automatically seeds default accounts and criteria on initial startup.*

### Step 3: Run the React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## 🔑 Default Development Credentials

| Role | Username / Email | Password | Quick-Fill Button |
|---|---|---|---|
| **Administrator** | `admin@projecteval.com` | `Admin@123` | Click **Admin** on login page |
| **Faculty Evaluator** | `evaluator@projecteval.com` | `Evaluator@123` | Click **Evaluator** on login page |
| **Student Developer** | `student@projecteval.com` | `Student@123` | Click **Student** on login page |

---

## 🧪 Testing

### Backend Unit & Integration Tests
```bash
cd backend
mvn test
```

### Automated End-to-End Workflow Verification
Run the complete integration suite verifying health, authentication, project submission, automated evaluation, manual review, and PDF report generation:
```bash
node test_workflow.mjs
```

---

## 📚 Complete Documentation

Comprehensive engineering documentation is available under [`docs/`](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/):
- **[SRS.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/SRS.md):** Software Requirements Specification (functional & non-functional requirements)
- **[LLD.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/LLD.md):** Low-Level Design (class diagrams, design patterns, sequence diagrams)
- **[ARCHITECTURE.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/ARCHITECTURE.md):** High-level system architecture and Docker sandbox isolation model
- **[DATABASE.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/DATABASE.md):** PostgreSQL Schema, relational constraints, and Mermaid ERD
- **[API.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/API.md):** Complete REST API endpoint reference and sample payloads
- **[TESTING.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/TESTING.md):** Multi-tier testing strategy, JUnit tests, and verification scripts
- **[DEPLOYMENT.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/DEPLOYMENT.md):** Containerization and production deployment guide
- **[SECURITY.md](file:///c:/Users/M.Premananda/OneDrive%20-%20Aseuro%20Technologies%20Private%20Limited/Desktop/Project%20evaluation%20system/Project-evaluation-system/docs/SECURITY.md):** Security hardening, sandbox isolation, and cryptographic protections

---

## 📄 License
This project is licensed under the MIT License.
