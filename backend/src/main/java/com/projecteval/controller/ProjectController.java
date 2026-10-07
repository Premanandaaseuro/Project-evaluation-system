package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.project.AssignEvaluatorRequest;
import com.projecteval.dto.project.ProjectCreateRequest;
import com.projecteval.dto.project.ProjectResponse;
import com.projecteval.model.Role;
import com.projecteval.security.UserPrincipal;
import com.projecteval.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<ProjectResponse> projects;
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));
        boolean isEvaluator = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Role.ROLE_EVALUATOR.name()));

        if (isAdmin) {
            projects = projectService.getAllProjects();
        } else if (isEvaluator) {
            projects = projectService.getAssignedProjects(currentUser.getId());
        } else {
            projects = projectService.getProjectsByStudent(currentUser.getId());
        }

        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.createProject(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse response = projectService.submitProject(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Project submitted successfully", response));
    }

    @PostMapping("/{id}/upload-zip")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<ProjectResponse>> uploadZip(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse response = projectService.uploadZip(id, currentUser.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("ZIP archive uploaded successfully", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignEvaluator(
            @PathVariable Long id,
            @Valid @RequestBody AssignEvaluatorRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ProjectResponse response = projectService.assignEvaluator(id, request.getEvaluatorId(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Evaluator assigned successfully", response));
    }
}
