package com.projecteval.config;

import com.projecteval.model.*;
import com.projecteval.repository.*;
import com.projecteval.service.EvaluationService;
import com.projecteval.service.ScoreEngine;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final EvaluationCriteriaRepository criteriaRepository;
    private final ProjectRepository projectRepository;
    private final EvaluatorAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EvaluationService evaluationService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded with user accounts. Skipping initial seed.");
            return;
        }

        log.info("Initializing ProjectEval seed data...");

        // 1. Seed Admin
        User admin = User.builder()
                .username("admin")
                .email("admin@projecteval.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);

        // 2. Seed Evaluator
        User evaluatorUser = User.builder()
                .username("evaluator")
                .email("evaluator@projecteval.com")
                .password(passwordEncoder.encode("Evaluator@123"))
                .role(Role.ROLE_EVALUATOR)
                .active(true)
                .build();
        User savedEvaluatorUser = userRepository.save(evaluatorUser);

        Evaluator evaluator = Evaluator.builder()
                .user(savedEvaluatorUser)
                .employeeCode("EVAL-101")
                .fullName("Dr. Sarah Jenkins")
                .department("Computer Science & Engineering")
                .build();
        Evaluator savedEvaluator = evaluatorRepository.save(evaluator);

        // 3. Seed Student
        User studentUser = User.builder()
                .username("student")
                .email("student@projecteval.com")
                .password(passwordEncoder.encode("Student@123"))
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();
        User savedStudentUser = userRepository.save(studentUser);

        Student student = Student.builder()
                .user(savedStudentUser)
                .studentCode("STU-2026-088")
                .fullName("Alex Rivera")
                .department("Computer Science & Engineering")
                .semester(8)
                .phone("+1-555-0199")
                .build();
        Student savedStudent = studentRepository.save(student);

        // 4. Seed Default Evaluation Rubric Criteria
        seedCriteria();

        // 5. Seed Sample Projects
        Project p1 = Project.builder()
                .student(savedStudent)
                .title("Employee Management System")
                .description("Full-stack enterprise application for staff attendance, department hierarchies, payroll calculation, and role-based access.")
                .repositoryUrl("https://github.com/example/employee-management-system")
                .projectType("Spring Boot & React")
                .technologyStack("Java 21, Spring Boot 3, PostgreSQL, React, Tailwind CSS, Docker")
                .requirements("1. User registration and secure login\n2. Admin should be able to add employees\n3. Employee should be able to update profile\n4. Department hierarchy and payroll calculation\n5. Admin should be able to archive employees")
                .status(ProjectStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(2))
                .build();
        Project savedP1 = projectRepository.save(p1);

        EvaluatorAssignment assign1 = EvaluatorAssignment.builder()
                .project(savedP1)
                .evaluator(savedEvaluator)
                .status("ASSIGNED")
                .build();
        assignmentRepository.save(assign1);

        // Run automated evaluation for sample project 1
        evaluationService.startEvaluation(savedP1.getId());

        Project p2 = Project.builder()
                .student(savedStudent)
                .title("Healthcare Appointment & Telehealth Platform")
                .description("Cloud-native microservices architecture enabling online doctor booking, video consults, prescription tracking, and billing.")
                .repositoryUrl("https://github.com/example/telehealth-platform")
                .projectType("Node.js & React")
                .technologyStack("TypeScript, Express, React, PostgreSQL, Redis, Docker")
                .requirements("1. Patient profile registration\n2. Doctor availability scheduling\n3. Real-time consultation booking\n4. Electronic prescription generation\n5. Payment gateway webhook processing")
                .status(ProjectStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusHours(8))
                .build();
        Project savedP2 = projectRepository.save(p2);

        EvaluatorAssignment assign2 = EvaluatorAssignment.builder()
                .project(savedP2)
                .evaluator(savedEvaluator)
                .status("ASSIGNED")
                .build();
        assignmentRepository.save(assign2);

        evaluationService.startEvaluation(savedP2.getId());

        log.info("ProjectEval seed data initialization completed successfully.");
    }

    private void seedCriteria() {
        criteriaRepository.saveAll(List.of(
                // Automated
                EvaluationCriteria.builder().name("Project Build & Startup").description("Build compilation, dependency resolution, application bootstrap").maxMarks(10).evaluationType(EvaluationType.AUTOMATED).criterionKey("BUILD_STARTUP").active(true).build(),
                EvaluationCriteria.builder().name("API Functionality").description("REST endpoints, HTTP status codes, CRUD operations, payload validation").maxMarks(20).evaluationType(EvaluationType.AUTOMATED).criterionKey("API_TESTING").active(true).build(),
                EvaluationCriteria.builder().name("UI Functionality").description("Page load, interactive controls, route guards, forms, no console errors").maxMarks(15).evaluationType(EvaluationType.AUTOMATED).criterionKey("UI_TESTING").active(true).build(),
                EvaluationCriteria.builder().name("Database & Integration").description("Connection pooling, relational schema, ACID transaction integrity").maxMarks(15).evaluationType(EvaluationType.AUTOMATED).criterionKey("DB_TESTING").active(true).build(),
                EvaluationCriteria.builder().name("Requirements Implementation").description("Coverage and verification of user-specified requirements").maxMarks(15).evaluationType(EvaluationType.AUTOMATED).criterionKey("REQUIREMENTS").active(true).build(),
                EvaluationCriteria.builder().name("Security").description("No hardcoded secrets or tokens, BCrypt hashing, secure headers").maxMarks(5).evaluationType(EvaluationType.AUTOMATED).criterionKey("SECURITY").active(true).build(),
                EvaluationCriteria.builder().name("Code Quality").description("Layered architecture, error handling, maintainability, naming").maxMarks(3).evaluationType(EvaluationType.AUTOMATED).criterionKey("CODE_QUALITY").active(true).build(),
                EvaluationCriteria.builder().name("Documentation").description("README completeness, setup instructions, API contracts").maxMarks(2).evaluationType(EvaluationType.AUTOMATED).criterionKey("DOCUMENTATION").active(true).build(),

                // Manual
                EvaluationCriteria.builder().name("Innovation").description("Novelty, creative problem solving, unique features").maxMarks(3).evaluationType(EvaluationType.MANUAL).criterionKey("INNOVATION").active(true).build(),
                EvaluationCriteria.builder().name("Technical Implementation").description("Engineering rigor, complex algorithms, architectural soundess").maxMarks(4).evaluationType(EvaluationType.MANUAL).criterionKey("TECHNICAL").active(true).build(),
                EvaluationCriteria.builder().name("Documentation").description("Clarity of design reports, architecture diagrams, user manuals").maxMarks(3).evaluationType(EvaluationType.MANUAL).criterionKey("DOC_MANUAL").active(true).build(),
                EvaluationCriteria.builder().name("Presentation").description("Code readability, demo flow, clarity of communication").maxMarks(2).evaluationType(EvaluationType.MANUAL).criterionKey("PRESENTATION").active(true).build(),
                EvaluationCriteria.builder().name("Final Outcome").description("Overall completeness, user experience polish, stability").maxMarks(3).evaluationType(EvaluationType.MANUAL).criterionKey("OUTCOME").active(true).build()
        ));
    }
}
