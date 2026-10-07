# Testing Strategy & Automated Test Suite
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Multi-Tier Testing Methodology

ProjectEval implements comprehensive multi-tier testing:

1. **Unit Testing (JUnit 5 & AssertJ):**
   - Validates business logic, scoring formulas, rubric limits, and password encryption without external dependencies.
2. **Service & Analyzer Testing:**
   - Tests detector accuracy against Java, Spring Boot, Node.js, and Python repositories.
   - Tests secret detection algorithms and regex pattern matching.
3. **API Integration Testing (HTTP / REST):**
   - End-to-end testing of authentication, role guards, project lifecycle, and PDF generation.
4. **Browser & UI Testing:**
   - Frontend component unit tests and automated E2E browser test workflows.

---

### 2. Executing Backend Tests

To run the JUnit 5 test suite:
```bash
cd backend
mvn test
```

Sample test output:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.projecteval.ScoreEngineTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.projecteval.SecurityAnalyzerTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

### 3. Executing End-to-End Workflow Verification

A complete automated workflow script `test_workflow.mjs` is provided in the repository root:
```bash
node test_workflow.mjs
```

The script exercises:
- System Health endpoint (`GET /api/health`)
- Administrator login (`admin@projecteval.com`)
- Admin dashboard KPI calculations
- Student login (`student@projecteval.com`)
- New project submission with custom requirements
- Admin evaluator assignment
- Level 1 Automated Sandbox Pipeline execution
- Evaluator login (`evaluator@projecteval.com`)
- Level 2 Manual scoring submission
- Scorecard calculation (100 Marks, Grade A+)
- Binary PDF report download and size verification

---

### 4. Playwright Browser Automation

Automated browser tests verify:
- Navigation across `/login`, `/register`, `/admin/dashboard`, `/student/dashboard`, and `/evaluator/dashboard`.
- Form validation error feedback tooltips.
- Interactive range sliders in the manual evaluation workspace.
- Responsive mobile drawer and desktop sidebar navigation.
