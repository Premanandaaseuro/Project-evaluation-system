# Software Requirements Specification (SRS)
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Introduction

#### 1.1 Purpose
The purpose of this document is to detail the software requirements for **ProjectEval**, a robust, production-quality automated project evaluation and manual grading platform. The system evaluates software projects submitted by students/developers across two distinct tiers: **Level 1 Automated Evaluation (85 Marks)** and **Level 2 Manual Faculty Evaluation (15 Marks)**, combining results into an institutional grade out of 100.

#### 1.2 Scope
ProjectEval provides:
- Secure JWT authentication with BCrypt encryption and role-based access control (Admin, Student, Evaluator).
- Automated detection and static/dynamic evaluation of React, Node.js, Java, Spring Boot, and Python applications.
- Sandboxed execution preventing host filesystem intrusion and resource starvation.
- Automated API functional testing, UI simulation, schema verification, security vulnerability analysis, and requirement coverage.
- Faculty evaluation portal with sliders and qualitative observation comments.
- Dynamic report generation exporting downloadable PDF, HTML, and JSON scorecards.
- Real-time audit trails and notification dispatch.

---

### 2. Overall Description

#### 2.1 User Roles & Personas
| Role | Description | Core Capabilities |
|---|---|---|
| **ADMIN** | System administrator / Department Head | Full administrative oversight, evaluator assignments, rubric configuration, audit inspection, report generation. |
| **STUDENT** | Project author / Candidate | Project submission (GitHub / ZIP), requirements declaration, tracking timeline, scorecard inspection, PDF downloads. |
| **EVALUATOR** | Faculty member / Senior reviewer | Portfolio review, automated test verification, manual rubric scoring (15 marks), feedback comments. |

#### 2.2 System Flow Diagram
```mermaid
graph TD
    Student[Student Developer] -->|Submits Project & Requirements| Platform[ProjectEval Core]
    Admin[Administrator] -->|Assigns Faculty| Platform
    Platform -->|Spawns Sandbox Workspace| Sandbox[Docker Sandbox Environment]
    Sandbox -->|Executes Build & Probes| Level1[Level 1 Automated Tests (85 Marks)]
    Level1 -->|Records Category Results| DB[(PostgreSQL Database)]
    Evaluator[Faculty Evaluator] -->|Inspects Automated Results & Logs| EvalPortal[Evaluator Workspace]
    EvalPortal -->|Submits Manual Marks (15 Marks)| ScoreEngine[Scoring & Grading Engine]
    ScoreEngine -->|Computes Total & Letter Grade| FinalResult[Final Scorecard (100 Marks)]
    FinalResult -->|Generates Binary PDF| PDF[Official Evaluation Report]
```

---

### 3. Functional Requirements

#### 3.1 Authentication & User Management (FR-AUTH)
- **FR-AUTH-1:** Password policy enforcement: minimum 8 characters, at least one uppercase, one lowercase, one number, and one special character.
- **FR-AUTH-2:** Passwords hashed with BCrypt (10 rounds). Plaintext passwords must never be persisted or logged.
- **FR-AUTH-3:** Stateless JWT authentication with expiration and role claims (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_EVALUATOR`).

#### 3.2 Submission Engine (FR-SUBMIT)
- **FR-SUBMIT-1:** Accept project title, description, technology stack, project type, and requirements list.
- **FR-SUBMIT-2:** Support repository submission via valid HTTP/HTTPS GitHub URLs.
- **FR-SUBMIT-3:** Support archive submission via multipart `.zip` uploads with ZIP Slip path traversal validation.

#### 3.3 Automated Evaluation Engine (FR-AUTO - 85 Marks)
- **FR-AUTO-1 (Build & Startup - 10 Marks):** Resolve dependencies, compile source, verify port binding and health check.
- **FR-AUTO-2 (API Functionality - 20 Marks):** Test discovered REST endpoints across GET, POST, PUT, DELETE with payload validation.
- **FR-AUTO-3 (UI Functionality - 15 Marks):** Verify DOM render, route guards, form validations, and console error cleanliness.
- **FR-AUTO-4 (Database & Integration - 15 Marks):** Inspect connection pool, relational entities, constraints, and ACID transactions.
- **FR-AUTO-5 (Requirements Implementation - 15 Marks):** Parse student's numbered requirements and evaluate feature coverage.
- **FR-AUTO-6 (Security - 5 Marks):** Scan code for hardcoded secrets, database credentials, exposed tokens, and cryptographic hashing.
- **FR-AUTO-7 (Code Quality - 3 Marks):** Evaluate layered architecture, exception handling, and naming conventions.
- **FR-AUTO-8 (Documentation - 2 Marks):** Inspect presence and depth of README.md and setup instructions.

#### 3.4 Manual Evaluation Engine (FR-MANUAL - 15 Marks)
- **FR-MAN-1:** Evaluators award:
  - Innovation & Novelty (Max 3 Marks)
  - Technical Implementation Rigor (Max 4 Marks)
  - Documentation Quality (Max 3 Marks)
  - Presentation & Demo Flow (Max 2 Marks)
  - Final Outcome & Polish (Max 3 Marks)
- **FR-MAN-2:** Evaluator comments recorded alongside marks.

#### 3.5 Grading & Scorecard (FR-GRADE)
- **FR-GRADE-1:** Final score calculated on backend: `Final = Automated + Manual` (Max 100).
- **FR-GRADE-2:** Grading Scale:
  - 90–100: A+ (Outstanding)
  - 80–89.9: A (Excellent)
  - 70–79.9: B (Very Good)
  - 60–69.9: C (Good)
  - 50–59.9: D (Pass)
  - < 50: Needs Improvement
- **FR-GRADE-3:** Server-side PDF export rendering student metadata, test tables, category scores, and faculty remarks.

---

### 4. Non-Functional Requirements

- **NFR-SECURITY:** Isolated Docker execution environment with non-root user, memory limits, and execution timeouts.
- **NFR-PERFORMANCE:** Background async execution prevents UI blocking during build and testing.
- **NFR-USABILITY:** Responsive React frontend with modern dark mode aesthetic, micro-animations, and clean typography.
- **NFR-AUDIT:** Every critical transaction logged to immutable audit trail.
