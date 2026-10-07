# Security Architecture & Hardening Guide
## Project: ProjectEval — Automated Project Evaluation & Testing Platform

### 1. Threat Model & Security Posture

ProjectEval handles unverified third-party code submitted by students. To maintain total host isolation and prevent malicious actions, multi-layer security protections are built into every component.

---

### 2. Sandbox Execution Hardening (Section 12 & 39)

1. **Host Isolation:**
   - Submitted projects never execute directly within the Spring Boot host JVM.
   - Every execution workspace is ephemeral and isolated under `./eval-workspace/project-{id}-{uuid}/`.
2. **Resource Constraints:**
   - **CPU Quota:** Limited to 1.0 vCPU.
   - **Memory Quota:** Capped at 512MB RAM with swap limits.
   - **Execution Timeout:** Enforced timeout of 120 seconds prevents runaway infinite loops.
3. **Privilege Demotion:**
   - Non-root user execution inside containers.
   - Host docker socket is NEVER mounted or exposed to student workspaces.
4. **Automatic Teardown:**
   - Regardless of build or test outcome, temporary workspaces are cleaned up recursively.

---

### 3. Archive & File Upload Protections (Section 11)

1. **ZIP Slip (Directory Traversal) Protection:**
   - Uploaded archives are inspected prior to extraction.
   - Any file name containing relative path sequences (`..`, `/`, `\`) or targeting absolute system paths is rejected immediately with HTTP 400.
2. **Size Limits:**
   - File uploads are capped at 50MB via Spring Boot multipart configuration.
   - MIME type and file extension verification rejects non-zip archives.

---

### 4. Authentication & Access Control (Section 4)

1. **Password Security:**
   - Passwords must be at least 8 characters long with uppercase, lowercase, number, and special character.
   - Passwords hashed using standard BCrypt (10 rounds). Plaintext passwords are never stored or logged.
2. **JWT Security:**
   - HMAC SHA-384 / SHA-256 signed tokens with 24-hour expiration.
   - Stateless session management eliminates CSRF vulnerability surfaces.
3. **Role-Based Authorization:**
   - `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` secures administrative endpoints.
   - Students cannot access or submit reviews on projects belonging to other students.
   - Evaluators cannot submit manual marks on unassigned projects.

---

### 5. Audit Logging (Section 26)

All critical events are captured in the immutable `audit_logs` table:
- User login / logout
- Project creation & submission
- Evaluator assignment
- Automated evaluation start / completion
- Manual evaluation submission
- Account status toggles
