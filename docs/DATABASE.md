# Database Design & Schema Reference
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o| STUDENTS : "has profile"
    USERS ||--o| EVALUATORS : "has profile"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ AUDIT_LOGS : "triggers"
    STUDENTS ||--o{ PROJECTS : "submits"
    PROJECTS ||--o{ EVALUATOR_ASSIGNMENTS : "assigned to"
    EVALUATORS ||--o{ EVALUATOR_ASSIGNMENTS : "undertakes"
    PROJECTS ||--o{ AUTOMATED_EVALUATIONS : "undergoes"
    AUTOMATED_EVALUATIONS ||--o{ AUTOMATED_TEST_RESULTS : "contains"
    PROJECTS ||--o{ MANUAL_EVALUATIONS : "reviewed in"
    EVALUATORS ||--o{ MANUAL_EVALUATIONS : "scores"
    PROJECTS ||--o| FINAL_RESULTS : "receives"

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar role
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    STUDENTS {
        bigint id PK
        bigint user_id FK
        varchar student_code UK
        varchar full_name
        varchar department
        int semester
        varchar phone
        timestamp created_at
    }

    EVALUATORS {
        bigint id PK
        bigint user_id FK
        varchar employee_code UK
        varchar full_name
        varchar department
        timestamp created_at
    }

    PROJECTS {
        bigint id PK
        bigint student_id FK
        varchar title
        text description
        varchar repository_url
        varchar project_type
        varchar technology_stack
        varchar documentation_path
        varchar zip_path
        text requirements
        varchar status
        timestamp submitted_at
        timestamp created_at
        timestamp updated_at
    }

    EVALUATOR_ASSIGNMENTS {
        bigint id PK
        bigint project_id FK
        bigint evaluator_id FK
        timestamp assigned_at
        varchar status
    }

    EVALUATION_CRITERIA {
        bigint id PK
        varchar name
        text description
        int max_marks
        varchar evaluation_type
        varchar criterion_key UK
        boolean active
    }

    AUTOMATED_EVALUATIONS {
        bigint id PK
        bigint project_id FK
        varchar build_status
        varchar startup_status
        varchar api_status
        varchar ui_status
        varchar database_status
        varchar security_status
        varchar code_quality_status
        varchar documentation_status
        double automated_score
        varchar job_status
        text build_logs
        text ai_feedback
        text error_message
        timestamp started_at
        timestamp completed_at
    }

    AUTOMATED_TEST_RESULTS {
        bigint id PK
        bigint evaluation_id FK
        varchar test_name
        varchar category
        text expected_result
        text actual_result
        varchar status
        bigint execution_time
        double marks_awarded
        double max_marks
    }

    MANUAL_EVALUATIONS {
        bigint id PK
        bigint project_id FK
        bigint evaluator_id FK
        double innovation_marks
        double technical_marks
        double documentation_marks
        double presentation_marks
        double outcome_marks
        double manual_total
        text comments
        timestamp submitted_at
    }

    FINAL_RESULTS {
        bigint id PK
        bigint project_id FK,UK
        double automated_score
        double manual_score
        double final_score
        varchar grade
        text remarks
        boolean published
        timestamp published_at
    }

    AUDIT_LOGS {
        bigint id PK
        bigint user_id
        varchar username
        varchar action
        varchar entity_type
        bigint entity_id
        timestamp timestamp
        text details
    }
```

---

### 2. Indexes and Foreign Key Constraints

1. **`users`**: Unique constraint on `username` and `email`.
2. **`students`**: Foreign key to `users(id)`, unique index on `student_code`.
3. **`evaluators`**: Foreign key to `users(id)`, unique index on `employee_code`.
4. **`projects`**: Foreign key to `students(id)`, index on `status`.
5. **`evaluator_assignments`**: Foreign keys to `projects(id)` and `evaluators(id)`.
6. **`automated_evaluations`**: Foreign key to `projects(id)`.
7. **`automated_test_results`**: Foreign key to `automated_evaluations(id)` with cascading delete.
8. **`manual_evaluations`**: Foreign keys to `projects(id)` and `evaluators(id)`.
9. **`final_results`**: Unique foreign key to `projects(id)`.
10. **`audit_logs`**: Ordered timestamp index for high performance descending pagination.
