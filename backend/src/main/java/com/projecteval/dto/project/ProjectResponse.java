package com.projecteval.dto.project;

import com.projecteval.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentDepartment;
    private String title;
    private String description;
    private String repositoryUrl;
    private String projectType;
    private String technologyStack;
    private String documentationPath;
    private String zipPath;
    private String requirements;
    private ProjectStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Assignment & Evaluation summary fields
    private Long assignedEvaluatorId;
    private String assignedEvaluatorName;
    private Double automatedScore;
    private Double manualScore;
    private Double finalScore;
    private String grade;
    private Boolean resultPublished;
}
