package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.evaluation.EvaluationDetailsResponse;
import com.projecteval.dto.evaluation.ManualEvaluationRequest;
import com.projecteval.model.AutomatedEvaluation;
import com.projecteval.model.FinalResult;
import com.projecteval.security.UserPrincipal;
import com.projecteval.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/project/{projectId}/run")
    public ResponseEntity<ApiResponse<AutomatedEvaluation>> runAutomatedEvaluation(@PathVariable Long projectId) {
        AutomatedEvaluation evaluation = evaluationService.startEvaluation(projectId);
        return ResponseEntity.ok(ApiResponse.success("Automated evaluation pipeline executed successfully", evaluation));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<EvaluationDetailsResponse>> getProjectEvaluationDetails(@PathVariable Long projectId) {
        EvaluationDetailsResponse details = evaluationService.getEvaluationDetails(projectId);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @PostMapping("/project/{projectId}/manual")
    @PreAuthorize("hasAnyAuthority('ROLE_EVALUATOR', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<FinalResult>> submitManualEvaluation(
            @PathVariable Long projectId,
            @Valid @RequestBody ManualEvaluationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FinalResult result = evaluationService.submitManualEvaluation(projectId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Manual evaluation submitted and final score calculated", result));
    }
}
