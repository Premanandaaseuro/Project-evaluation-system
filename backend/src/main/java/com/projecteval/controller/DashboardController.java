package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.dashboard.AdminDashboardDto;
import com.projecteval.dto.dashboard.EvaluatorDashboardDto;
import com.projecteval.dto.dashboard.StudentDashboardDto;
import com.projecteval.security.UserPrincipal;
import com.projecteval.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard() {
        AdminDashboardDto dto = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<StudentDashboardDto>> getStudentDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        StudentDashboardDto dto = dashboardService.getStudentDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/evaluator")
    @PreAuthorize("hasAnyAuthority('ROLE_EVALUATOR', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EvaluatorDashboardDto>> getEvaluatorDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        EvaluatorDashboardDto dto = dashboardService.getEvaluatorDashboard(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
