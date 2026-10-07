package com.projecteval.dto.dashboard;

import com.projecteval.dto.project.ProjectResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private long totalStudents;
    private long totalEvaluators;
    private long totalProjects;
    private long submittedProjects;
    private long pendingEvaluations;
    private long completedEvaluations;
    private double averageScore;
    private double highestScore;
    private double lowestScore;

    // Charts & Analytics
    private Map<String, Long> statusDistribution;
    private Map<String, Long> gradeDistribution;
    private Map<String, Double> departmentAverages;
    private Map<String, Object> scoreComparison; // avg automated vs avg manual

    private List<ProjectResponse> recentProjects;
}
