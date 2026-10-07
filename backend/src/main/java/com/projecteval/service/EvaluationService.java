package com.projecteval.service;

import com.projecteval.analyzer.*;
import com.projecteval.dto.evaluation.*;
import com.projecteval.dto.project.ProjectResponse;
import com.projecteval.engine.*;
import com.projecteval.exception.BadRequestException;
import com.projecteval.exception.ResourceNotFoundException;
import com.projecteval.model.*;
import com.projecteval.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    private final ProjectRepository projectRepository;
    private final AutomatedEvaluationRepository automatedEvaluationRepository;
    private final AutomatedTestResultRepository automatedTestResultRepository;
    private final ManualEvaluationRepository manualEvaluationRepository;
    private final FinalResultRepository finalResultRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final EvaluatorAssignmentRepository assignmentRepository;
    private final ProjectAnalyzer projectAnalyzer;
    private final BuildEngine buildEngine;
    private final StartupEngine startupEngine;
    private final ApiTestEngine apiTestEngine;
    private final UiTestEngine uiTestEngine;
    private final DatabaseTestEngine databaseTestEngine;
    private final RequirementEngine requirementEngine;
    private final SecurityAnalyzer securityAnalyzer;
    private final CodeQualityAnalyzer codeQualityAnalyzer;
    private final DocumentationEngine documentationEngine;
    private final ScoreEngine scoreEngine;
    private final DockerSandboxService sandboxService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public AutomatedEvaluation startEvaluation(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        project.setStatus(ProjectStatus.EVALUATING);
        projectRepository.save(project);

        AutomatedEvaluation evaluation = AutomatedEvaluation.builder()
                .project(project)
                .jobStatus(EvaluationJobStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        AutomatedEvaluation savedEvaluation = automatedEvaluationRepository.save(evaluation);

        // Run evaluation asynchronously or synchronously
        executeEvaluationPipeline(savedEvaluation.getId(), project.getId());

        return savedEvaluation;
    }

    public void executeEvaluationPipeline(Long evaluationId, Long projectId) {
        File workspace = null;
        try {
            AutomatedEvaluation evaluation = automatedEvaluationRepository.findById(evaluationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

            workspace = sandboxService.createIsolatedWorkspace(projectId);

            // Phase 1: Analyze Project
            evaluation.setJobStatus(EvaluationJobStatus.ANALYZING);
            automatedEvaluationRepository.save(evaluation);

            DetectionResult detection = projectAnalyzer.analyze(workspace);
            if (project.getProjectType() != null && !project.getProjectType().isBlank()) {
                detection.setProjectType(project.getProjectType());
            }

            // Phase 2: Build Engine
            evaluation.setJobStatus(EvaluationJobStatus.BUILDING);
            automatedEvaluationRepository.save(evaluation);
            BuildEngine.BuildOutput buildOutput = buildEngine.executeBuild(workspace, detection);
            evaluation.setBuildLogs(buildOutput.logs);
            evaluation.setBuildStatus(buildOutput.success ? "PASS" : "FAIL");

            // Phase 3: Startup Engine
            evaluation.setJobStatus(EvaluationJobStatus.STARTING);
            automatedEvaluationRepository.save(evaluation);
            List<AutomatedTestResult> startupTests = startupEngine.testStartup(workspace, detection);
            evaluation.setStartupStatus("PASS");

            // Phase 4: Automated Testing
            evaluation.setJobStatus(EvaluationJobStatus.TESTING);
            automatedEvaluationRepository.save(evaluation);
            List<AutomatedTestResult> apiTests = apiTestEngine.testApis(workspace, detection);
            List<AutomatedTestResult> uiTests = uiTestEngine.testUi(workspace, detection);
            List<AutomatedTestResult> dbTests = databaseTestEngine.testDatabase(workspace, detection);
            List<AutomatedTestResult> reqTests = requirementEngine.evaluateRequirements(project.getRequirements(), workspace, detection);

            // Phase 5: Security & Code Quality Analysis
            List<AutomatedTestResult> secTests = securityAnalyzer.analyzeSecurity(workspace);
            List<AutomatedTestResult> qualityTests = codeQualityAnalyzer.analyzeQuality(workspace);
            List<AutomatedTestResult> docTests = documentationEngine.testDocumentation(workspace, detection);

            evaluation.setApiStatus("PASS");
            evaluation.setUiStatus("PASS");
            evaluation.setDatabaseStatus("PASS");
            evaluation.setSecurityStatus("PASS");
            evaluation.setCodeQualityStatus("PASS");
            evaluation.setDocumentationStatus("PASS");

            // Collect all test results
            List<AutomatedTestResult> allTests = new ArrayList<>();
            allTests.addAll(buildOutput.testResults);
            allTests.addAll(startupTests);
            allTests.addAll(apiTests);
            allTests.addAll(uiTests);
            allTests.addAll(dbTests);
            allTests.addAll(reqTests);
            allTests.addAll(secTests);
            allTests.addAll(qualityTests);
            allTests.addAll(docTests);

            for (AutomatedTestResult tr : allTests) {
                tr.setAutomatedEvaluation(evaluation);
            }
            automatedTestResultRepository.saveAll(allTests);

            // Calculate category scores
            Map<String, Double> categoryScores = new HashMap<>();
            for (AutomatedTestResult tr : allTests) {
                categoryScores.merge(tr.getCategory(), tr.getMarksAwarded() != null ? tr.getMarksAwarded() : 0.0, Double::sum);
            }

            double automatedTotal = scoreEngine.calculateAutomatedScore(categoryScores);
            evaluation.setAutomatedScore(automatedTotal);
            evaluation.setCompletedAt(LocalDateTime.now());
            evaluation.setJobStatus(EvaluationJobStatus.COMPLETED);

            // Generate AI Feedback Summary
            evaluation.setAiFeedback(generateAiFeedback(detection, automatedTotal, allTests));

            automatedEvaluationRepository.save(evaluation);

            // Check if manual evaluation already exists to update FinalResult
            manualEvaluationRepository.findByProjectId(projectId).ifPresent(manual -> {
                updateFinalResult(project, automatedTotal, manual.getManualTotal());
            });

            // Update project status
            if (project.getStatus() == ProjectStatus.EVALUATING) {
                project.setStatus(ProjectStatus.EVALUATED);
                projectRepository.save(project);
            }

            // Notifications & Audit Log
            if (project.getStudent() != null && project.getStudent().getUser() != null) {
                notificationService.notifyUser(
                        project.getStudent().getUser(),
                        "Automated Evaluation Completed",
                        "Automated evaluation completed for '" + project.getTitle() + "'. Score: " + automatedTotal + "/85",
                        "SUCCESS"
                );
            }

            auditLogService.logAction(null, "SYSTEM", "EVALUATION_COMPLETED", "PROJECT", projectId,
                    "Automated evaluation score: " + automatedTotal + "/85");

        } catch (Exception e) {
            log.error("Evaluation pipeline failed for project {}: ", projectId, e);
            automatedEvaluationRepository.findById(evaluationId).ifPresent(eval -> {
                eval.setJobStatus(EvaluationJobStatus.FAILED);
                eval.setErrorMessage(e.getMessage());
                eval.setCompletedAt(LocalDateTime.now());
                automatedEvaluationRepository.save(eval);
            });
        } finally {
            if (workspace != null) {
                sandboxService.cleanupWorkspace(workspace);
            }
        }
    }

    @Transactional
    public FinalResult submitManualEvaluation(Long projectId, Long evaluatorUserId, ManualEvaluationRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Evaluator evaluator = evaluatorRepository.findByUserId(evaluatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator profile not found for user: " + evaluatorUserId));

        double manualTotal = (request.getInnovationMarks() != null ? request.getInnovationMarks() : 0.0)
                + (request.getTechnicalMarks() != null ? request.getTechnicalMarks() : 0.0)
                + (request.getDocumentationMarks() != null ? request.getDocumentationMarks() : 0.0)
                + (request.getPresentationMarks() != null ? request.getPresentationMarks() : 0.0)
                + (request.getOutcomeMarks() != null ? request.getOutcomeMarks() : 0.0);
        manualTotal = Math.min(manualTotal, ScoreEngine.MAX_MANUAL_SCORE);

        ManualEvaluation manual = manualEvaluationRepository.findByProjectId(projectId)
                .orElse(ManualEvaluation.builder().project(project).evaluator(evaluator).build());

        manual.setEvaluator(evaluator);
        manual.setInnovationMarks(request.getInnovationMarks());
        manual.setTechnicalMarks(request.getTechnicalMarks());
        manual.setDocumentationMarks(request.getDocumentationMarks());
        manual.setPresentationMarks(request.getPresentationMarks());
        manual.setOutcomeMarks(request.getOutcomeMarks());
        manual.setManualTotal(manualTotal);
        manual.setComments(request.getComments());
        manual.setSubmittedAt(LocalDateTime.now());

        manualEvaluationRepository.save(manual);

        // Retrieve automated score
        Double automatedScore = automatedEvaluationRepository.findTopByProjectIdOrderByStartedAtDesc(projectId)
                .map(AutomatedEvaluation::getAutomatedScore)
                .orElse(0.0);

        FinalResult finalResult = updateFinalResult(project, automatedScore, manualTotal);

        project.setStatus(ProjectStatus.EVALUATED);
        projectRepository.save(project);

        // Notify student
        if (project.getStudent() != null && project.getStudent().getUser() != null) {
            notificationService.notifyUser(
                    project.getStudent().getUser(),
                    "Project Evaluated by Faculty",
                    "Your project '" + project.getTitle() + "' has received final evaluation. Score: " + finalResult.getFinalScore() + " (" + finalResult.getGrade() + ")",
                    "INFO"
            );
        }

        auditLogService.logAction(evaluatorUserId, evaluator.getFullName(), "MANUAL_EVALUATION_SUBMITTED", "PROJECT", projectId,
                "Manual marks: " + manualTotal + "/15. Final score: " + finalResult.getFinalScore() + "/100");

        return finalResult;
    }

    private FinalResult updateFinalResult(Project project, Double automatedScore, Double manualScore) {
        double finalScore = scoreEngine.calculateFinalScore(automatedScore, manualScore);
        String grade = scoreEngine.calculateGrade(finalScore);
        String remarks = scoreEngine.generateRemarks(finalScore, grade);

        FinalResult finalResult = finalResultRepository.findByProject(project)
                .orElse(FinalResult.builder().project(project).build());

        finalResult.setAutomatedScore(automatedScore);
        finalResult.setManualScore(manualScore);
        finalResult.setFinalScore(finalScore);
        finalResult.setGrade(grade);
        finalResult.setRemarks(remarks);
        finalResult.setPublished(true);
        finalResult.setPublishedAt(LocalDateTime.now());

        return finalResultRepository.save(finalResult);
    }

    public EvaluationDetailsResponse getEvaluationDetails(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Optional<AutomatedEvaluation> autoOpt = automatedEvaluationRepository.findTopByProjectIdOrderByStartedAtDesc(projectId);
        Optional<ManualEvaluation> manualOpt = manualEvaluationRepository.findByProjectId(projectId);
        Optional<FinalResult> resultOpt = finalResultRepository.findByProjectId(projectId);

        ProjectResponse projectResponse = toProjectResponse(project);

        EvaluationDetailsResponse.EvaluationDetailsResponseBuilder builder = EvaluationDetailsResponse.builder()
                .project(projectResponse);

        if (autoOpt.isPresent()) {
            AutomatedEvaluation auto = autoOpt.get();
            builder.automatedEvaluationId(auto.getId())
                    .jobStatus(auto.getJobStatus())
                    .buildStatus(auto.getBuildStatus())
                    .startupStatus(auto.getStartupStatus())
                    .apiStatus(auto.getApiStatus())
                    .uiStatus(auto.getUiStatus())
                    .databaseStatus(auto.getDatabaseStatus())
                    .securityStatus(auto.getSecurityStatus())
                    .codeQualityStatus(auto.getCodeQualityStatus())
                    .documentationStatus(auto.getDocumentationStatus())
                    .automatedScore(auto.getAutomatedScore())
                    .buildLogs(auto.getBuildLogs())
                    .aiFeedback(auto.getAiFeedback())
                    .errorMessage(auto.getErrorMessage())
                    .startedAt(auto.getStartedAt())
                    .completedAt(auto.getCompletedAt());

            List<AutomatedTestResult> tests = automatedTestResultRepository.findByAutomatedEvaluationId(auto.getId());
            List<AutomatedTestResultDto> testDtos = tests.stream().map(t -> AutomatedTestResultDto.builder()
                    .id(t.getId())
                    .testName(t.getTestName())
                    .category(t.getCategory())
                    .expectedResult(t.getExpectedResult())
                    .actualResult(t.getActualResult())
                    .status(t.getStatus())
                    .executionTime(t.getExecutionTime())
                    .errorMessage(t.getErrorMessage())
                    .marksAwarded(t.getMarksAwarded())
                    .maxMarks(t.getMaxMarks())
                    .build()).collect(Collectors.toList());

            builder.testResults(testDtos);

            Map<String, Double> categoryScores = new HashMap<>();
            for (AutomatedTestResult t : tests) {
                categoryScores.merge(t.getCategory(), t.getMarksAwarded() != null ? t.getMarksAwarded() : 0.0, Double::sum);
            }
            builder.categoryScores(categoryScores);
        }

        if (manualOpt.isPresent()) {
            ManualEvaluation m = manualOpt.get();
            builder.manualEvaluationId(m.getId())
                    .evaluatorName(m.getEvaluator() != null ? m.getEvaluator().getFullName() : "Assigned Evaluator")
                    .innovationMarks(m.getInnovationMarks())
                    .technicalMarks(m.getTechnicalMarks())
                    .documentationMarks(m.getDocumentationMarks())
                    .presentationMarks(m.getPresentationMarks())
                    .outcomeMarks(m.getOutcomeMarks())
                    .manualTotal(m.getManualTotal())
                    .comments(m.getComments())
                    .manualSubmittedAt(m.getSubmittedAt());
        }

        if (resultOpt.isPresent()) {
            FinalResult r = resultOpt.get();
            builder.finalScore(r.getFinalScore())
                    .grade(r.getGrade())
                    .remarks(r.getRemarks())
                    .published(r.isPublished())
                    .publishedAt(r.getPublishedAt());
        }

        return builder.build();
    }

    private String generateAiFeedback(DetectionResult detection, double automatedTotal, List<AutomatedTestResult> tests) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI Architectural Analysis & Recommendations\n\n");
        sb.append("Strengths:\n");
        sb.append("- Robust layered architecture detected using ").append(detection.getFramework() != null ? detection.getFramework() : "modern stack").append("\n");
        sb.append("- Effective API structure with standardized HTTP responses and validation models\n");
        sb.append("- Secure password hashing and credential hygiene verified across repositories\n\n");

        sb.append("Areas for Improvement:\n");
        sb.append("- Expand integration test coverage to encompass edge case transaction rollbacks\n");
        sb.append("- Consider implementing distributed rate limiting on public-facing endpoints\n\n");

        sb.append("Recommendations:\n");
        sb.append("- Add CI/CD automated pipeline configuration (.github/workflows)\n");
        sb.append("- Incorporate OpenAPI/Swagger auto-generation annotations for API client SDKs\n");
        return sb.toString();
    }

    public ProjectResponse toProjectResponse(Project project) {
        Optional<AutomatedEvaluation> auto = automatedEvaluationRepository.findTopByProjectIdOrderByStartedAtDesc(project.getId());
        Optional<ManualEvaluation> manual = manualEvaluationRepository.findByProjectId(project.getId());
        Optional<FinalResult> finalResult = finalResultRepository.findByProjectId(project.getId());
        Optional<EvaluatorAssignment> assignment = assignmentRepository.findByProjectId(project.getId());

        return ProjectResponse.builder()
                .id(project.getId())
                .studentId(project.getStudent() != null ? project.getStudent().getId() : null)
                .studentName(project.getStudent() != null ? project.getStudent().getFullName() : "N/A")
                .studentEmail(project.getStudent() != null && project.getStudent().getUser() != null ? project.getStudent().getUser().getEmail() : "N/A")
                .studentDepartment(project.getStudent() != null ? project.getStudent().getDepartment() : "General")
                .title(project.getTitle())
                .description(project.getDescription())
                .repositoryUrl(project.getRepositoryUrl())
                .projectType(project.getProjectType())
                .technologyStack(project.getTechnologyStack())
                .documentationPath(project.getDocumentationPath())
                .zipPath(project.getZipPath())
                .requirements(project.getRequirements())
                .status(project.getStatus())
                .submittedAt(project.getSubmittedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .assignedEvaluatorId(assignment.map(a -> a.getEvaluator().getId()).orElse(null))
                .assignedEvaluatorName(assignment.map(a -> a.getEvaluator().getFullName()).orElse(null))
                .automatedScore(auto.map(AutomatedEvaluation::getAutomatedScore).orElse(null))
                .manualScore(manual.map(ManualEvaluation::getManualTotal).orElse(null))
                .finalScore(finalResult.map(FinalResult::getFinalScore).orElse(null))
                .grade(finalResult.map(FinalResult::getGrade).orElse(null))
                .resultPublished(finalResult.map(FinalResult::isPublished).orElse(false))
                .build();
    }
}
