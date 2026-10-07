package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.evaluation.EvaluationDetailsResponse;
import com.projecteval.service.EvaluationService;
import com.projecteval.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final EvaluationService evaluationService;

    @GetMapping("/{projectId}/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable Long projectId) {
        byte[] pdfBytes = reportService.generatePdfReport(projectId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ProjectEval-Report-Proj" + projectId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/{projectId}/json")
    public ResponseEntity<ApiResponse<EvaluationDetailsResponse>> getJsonReport(@PathVariable Long projectId) {
        EvaluationDetailsResponse details = evaluationService.getEvaluationDetails(projectId);
        return ResponseEntity.ok(ApiResponse.success(details));
    }
}
