# Architecture Design Document
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. High-Level System Architecture

```mermaid
graph TB
    subgraph Client Layer
        Browser[Modern Web Browser - Chrome, Firefox, Safari, Edge]
        ReactSPA[React 18 + TypeScript + Vite SPA]
        Tailwind[Tailwind CSS Design System]
        Browser --> ReactSPA
        ReactSPA --- Tailwind
    end

    subgraph Reverse Proxy / Web Tier
        Nginx[Nginx Edge Server :3000 / Vite Dev :5173]
        ReactSPA -->|REST API Calls| Nginx
    end

    subgraph Application Server [Spring Boot 3.x / Java 21]
        Security[Spring Security & JWT Filter]
        Controllers[REST Controllers]
        Services[Business Logic & Scoring Engine]
        DataLayer[Spring Data JPA & Hibernate]
        
        Nginx -->|Port 8080| Security
        Security --> Controllers
        Controllers --> Services
        Services --> DataLayer
    end

    subgraph Sandboxed Execution Cluster
        SandboxMgr[Docker Sandbox Manager]
        Container1[Isolated Container: Build & Run]
        Container2[Ephemeral Volumes & Network Guards]
        
        Services -->|Orchestrates| SandboxMgr
        SandboxMgr --> Container1
        Container1 --- Container2
    end

    subgraph Persistence Layer
        PostgreSQL[(PostgreSQL 16 Database)]
        DataLayer -->|Port 5432 / 5433| PostgreSQL
    end
```

---

### 2. Docker Sandbox Isolation Architecture

Executing user-provided code introduces significant security hazards. ProjectEval enforces a zero-trust model:

```
Uploaded Project (GitHub URL or ZIP)
           │
           ▼
[Security Gate: ZIP Slip Sanitization]
           │
           ▼
[Ephemeral Staging Directory]
           │
           ▼
[Docker Container Isolation]
 ├── Non-root runtime user
 ├── Resource Quota: 512MB RAM, 1.0 vCPU
 ├── Execution Timeout: 120 seconds hard kill
 ├── Read-only host filesystem mount
 └── Blocked access to host docker socket
           │
           ▼
[Automated Test Probes: Build, Startup, API, UI, DB, Sec]
           │
           ▼
[Teardown: Recursive Cleanup of Container & Temporary Volumes]
```

---

### 3. Frontend Architecture

The frontend follows an accessible, modular structure:
```
frontend/src/
├── components/        # Reusable UI widgets (Scorecard, Badges, Navbar, Sidebar)
├── context/           # State providers (AuthContext, Notifications)
├── pages/             # Route views segregated by domain:
│   ├── auth/          # Login, Register
│   ├── admin/         # Dashboard, Projects, Students, Evaluators, Settings, Audits
│   ├── student/       # Dashboard, Submit, MyProjects, ProjectDetails
│   └── evaluator/     # Dashboard, EvaluateWorkspace
├── services/          # Axios HTTP clients and interceptors
└── types/             # TypeScript interfaces matching backend models
```
