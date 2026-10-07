package com.projecteval.dto.evaluation;

import com.projecteval.dto.project.ProjectResponse;
import com.projecteval.model.EvaluationJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetailsResponse {
    private ProjectResponse project;

    // Automated Evaluation Overview
    private Long automatedEvaluationId;
    private EvaluationJobStatus jobStatus;
    private String buildStatus;
    private String startupStatus;
    private String apiStatus;
    private String uiStatus;
    private String databaseStatus;
    private String securityStatus;
    private String codeQualityStatus;
    private String documentationStatus;
    private Double automatedScore; // out of 85
    private String buildLogs;
    private String aiFeedback;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Categorized breakdown scores
    private Map<String, Double> categoryScores;

    // Individual test results
    private List<AutomatedTestResultDto> testResults;

    // Manual Evaluation Overview
    private Long manualEvaluationId;
    private String evaluatorName;
    private Double innovationMarks; // out of 3
    private Double technicalMarks; // out of 4
    private Double documentationMarks; // out of 3
    private Double presentationMarks; // out of 2
    private Double outcomeMarks; // out of 3
    private Double manualTotal; // out of 15
    private String comments;
    private LocalDateTime manualSubmittedAt;

    // Final Combined Scorecard
    private Double finalScore; // out of 100
    private String grade;
    private String remarks;
    private Boolean published;
    private LocalDateTime publishedAt;
}
