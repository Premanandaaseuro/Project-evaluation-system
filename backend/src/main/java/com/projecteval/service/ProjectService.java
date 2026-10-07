package com.projecteval.service;

import com.projecteval.dto.project.AssignEvaluatorRequest;
import com.projecteval.dto.project.ProjectCreateRequest;
import com.projecteval.dto.project.ProjectResponse;
import com.projecteval.exception.BadRequestException;
import com.projecteval.exception.ResourceNotFoundException;
import com.projecteval.model.*;
import com.projecteval.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final StudentRepository studentRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final EvaluatorAssignmentRepository assignmentRepository;
    private final EvaluationService evaluationService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public ProjectResponse createProject(Long studentUserId, ProjectCreateRequest request) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + studentUserId));

        validateRepositoryUrl(request.getRepositoryUrl());

        Project project = Project.builder()
                .student(student)
                .title(request.getTitle())
                .description(request.getDescription())
                .repositoryUrl(request.getRepositoryUrl())
                .projectType(request.getProjectType() != null ? request.getProjectType() : "React")
                .technologyStack(request.getTechnologyStack())
                .requirements(request.getRequirements())
                .status(ProjectStatus.DRAFT)
                .build();

        Project savedProject = projectRepository.save(project);

        auditLogService.logAction(studentUserId, student.getFullName(), "CREATE_PROJECT", "PROJECT", savedProject.getId(),
                "Created project: " + savedProject.getTitle());

        return evaluationService.toProjectResponse(savedProject);
    }

    @Transactional
    public ProjectResponse submitProject(Long projectId, Long studentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getStudent().getUser().getId().equals(studentUserId)) {
            throw new BadRequestException("You can only submit your own projects");
        }

        project.setStatus(ProjectStatus.SUBMITTED);
        project.setSubmittedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);

        auditLogService.logAction(studentUserId, project.getStudent().getFullName(), "SUBMIT_PROJECT", "PROJECT", saved.getId(),
                "Submitted project: " + saved.getTitle());

        // Notify Admins
        List<User> admins = userRepository.findByRole(Role.ROLE_ADMIN);
        for (User admin : admins) {
            notificationService.notifyUser(admin, "New Project Submission",
                    "Student " + project.getStudent().getFullName() + " submitted '" + project.getTitle() + "'", "INFO");
        }

        // Notify Student
        notificationService.notifyUser(project.getStudent().getUser(), "Project Submitted Successfully",
                "Your project '" + project.getTitle() + "' was submitted and is pending review.", "SUCCESS");

        return evaluationService.toProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse uploadZip(Long projectId, Long studentUserId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getStudent().getUser().getId().equals(studentUserId)) {
            throw new BadRequestException("You can only upload files to your own projects");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new BadRequestException("Only .zip files are permitted");
        }

        // Path Traversal Security check
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Dangerous file name detected");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String storedFileName = "proj_" + projectId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".zip";
            Path targetPath = Paths.get(uploadDir).resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            project.setZipPath(targetPath.toAbsolutePath().toString());
            Project saved = projectRepository.save(project);

            auditLogService.logAction(studentUserId, project.getStudent().getFullName(), "UPLOAD_ZIP", "PROJECT", saved.getId(),
                    "Uploaded project zip: " + originalFilename + " (" + file.getSize() + " bytes)");

            return evaluationService.toProjectResponse(saved);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Could not store project file: " + e.getMessage());
        }
    }

    @Transactional
    public ProjectResponse assignEvaluator(Long projectId, Long evaluatorId, Long adminUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Evaluator evaluator = evaluatorRepository.findById(evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found with id: " + evaluatorId));

        EvaluatorAssignment assignment = assignmentRepository.findByProjectId(projectId)
                .orElse(EvaluatorAssignment.builder().project(project).evaluator(evaluator).build());

        assignment.setEvaluator(evaluator);
        assignmentRepository.save(assignment);

        if (project.getStatus() == ProjectStatus.SUBMITTED || project.getStatus() == ProjectStatus.DRAFT) {
            project.setStatus(ProjectStatus.ASSIGNED);
            projectRepository.save(project);
        }

        // Notify Evaluator
        if (evaluator.getUser() != null) {
            notificationService.notifyUser(evaluator.getUser(), "New Project Assigned",
                    "You have been assigned to evaluate '" + project.getTitle() + "'", "INFO");
        }

        auditLogService.logAction(adminUserId, "ADMIN", "ASSIGN_EVALUATOR", "PROJECT", projectId,
                "Assigned evaluator " + evaluator.getFullName() + " to project " + project.getTitle());

        return evaluationService.toProjectResponse(project);
    }

    public List<ProjectResponse> getProjectsByStudent(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for user: " + studentUserId));
        return projectRepository.findByStudent(student).stream()
                .map(evaluationService::toProjectResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getAssignedProjects(Long evaluatorUserId) {
        Evaluator evaluator = evaluatorRepository.findByUserId(evaluatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not found for user: " + evaluatorUserId));

        List<EvaluatorAssignment> assignments = assignmentRepository.findByEvaluator(evaluator);
        return assignments.stream()
                .map(a -> evaluationService.toProjectResponse(a.getProject()))
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(evaluationService::toProjectResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return evaluationService.toProjectResponse(project);
    }

    private void validateRepositoryUrl(String url) {
        if (url != null && !url.isBlank()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new BadRequestException("Repository URL must begin with http:// or https://");
            }
        }
    }
}
