package com.projecteval.dto.dashboard;

import com.projecteval.dto.project.ProjectResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatorDashboardDto {
    private long totalAssigned;
    private long pendingEvaluations;
    private long completedEvaluations;
    private List<ProjectResponse> assignedProjects;
}
