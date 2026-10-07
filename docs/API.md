# REST API Specification
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Overview & Authentication
All secured endpoints require the HTTP header:
```http
Authorization: Bearer <jwt_access_token>
```
Standard JSON envelope returned by all endpoints:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

---

### 2. Authentication Endpoints

#### `POST /api/auth/login`
Authenticates a user and returns an access token.
- **Request Body:**
```json
{
  "usernameOrEmail": "admin@projecteval.com",
  "password": "Admin@123"
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@projecteval.com",
    "role": "ROLE_ADMIN",
    "fullName": "System Administrator"
  }
}
```

#### `POST /api/auth/register`
Registers a new student or evaluator account.
- **Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Password@123",
  "fullName": "John Doe",
  "role": "ROLE_STUDENT",
  "department": "Computer Science",
  "studentCode": "STU-2026-999",
  "semester": 8,
  "phone": "+1-555-0100"
}
```

---

### 3. Projects Endpoints

#### `GET /api/projects`
Returns projects based on the caller's role:
- **Admin:** All projects in system.
- **Evaluator:** Projects assigned to evaluator.
- **Student:** Projects authored by caller.

#### `POST /api/projects`
(Role: `ROLE_STUDENT`) Creates a new project draft.
- **Request Body:**
```json
{
  "title": "Cloud Microservices API Gateway",
  "description": "High-throughput reverse proxy with rate limiting and circuit breakers.",
  "projectType": "Spring Boot",
  "technologyStack": "Java 21, Spring Boot 3, Redis, PostgreSQL",
  "repositoryUrl": "https://github.com/example/api-gateway",
  "requirements": "1. User authentication\n2. Rate limiting\n3. Circuit breaker\n4. Routing\n5. Logging"
}
```

#### `POST /api/projects/{id}/upload-zip`
(Role: `ROLE_STUDENT`) Uploads project ZIP archive using `multipart/form-data`.

#### `POST /api/projects/{id}/submit`
(Role: `ROLE_STUDENT`) Transitions project state from `DRAFT` to `SUBMITTED`.

#### `POST /api/projects/{id}/assign`
(Role: `ROLE_ADMIN`) Assigns a faculty evaluator.
- **Request Body:**
```json
{
  "evaluatorId": 1
}
```

---

### 4. Evaluation Endpoints

#### `POST /api/evaluations/project/{projectId}/run`
Triggers the Level 1 automated evaluation pipeline in the isolated sandbox.

#### `GET /api/evaluations/project/{projectId}`
Retrieves the comprehensive scorecard, automated test items, logs, and manual faculty marks.

#### `POST /api/evaluations/project/{projectId}/manual`
(Role: `ROLE_EVALUATOR`, `ROLE_ADMIN`) Submits Level 2 manual faculty evaluation marks out of 15.
- **Request Body:**
```json
{
  "innovationMarks": 3.0,
  "technicalMarks": 4.0,
  "documentationMarks": 3.0,
  "presentationMarks": 2.0,
  "outcomeMarks": 3.0,
  "comments": "Excellent microservice architecture and comprehensive automated test suite."
}
```

---

### 5. Reports Endpoints

#### `GET /api/reports/{projectId}/pdf`
Downloads the official vector PDF scorecard report.
- **Content-Type:** `application/pdf`
- **Content-Disposition:** `attachment; filename=ProjectEval-Report-Proj{id}.pdf`

#### `GET /api/reports/{projectId}/json`
Returns full structured evaluation JSON representation.

---

### 6. Dashboard Endpoints

- `GET /api/dashboard/admin` (Admin KPI metrics, charts, and grade distribution)
- `GET /api/dashboard/student` (Student submission timeline and project status)
- `GET /api/dashboard/evaluator` (Evaluator assigned portfolio and review queue)

---

### 7. Audit & Configuration Endpoints

- `GET /api/audit-logs` (Immutable system activity logs)
- `GET /api/config/criteria` (Rubric marks breakdown)
- `PUT /api/config/criteria/{id}` (Update criteria marks)
- `GET /api/config/grading-scale` (Grade boundary mapping)
- `GET /api/health` (Health and version check)
