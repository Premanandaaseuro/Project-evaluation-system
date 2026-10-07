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
public class StudentDashboardDto {
    private long totalProjects;
    private long submittedProjects;
    private long evaluatedProjects;
    private Double averageScore;
    private List<ProjectResponse> projects;
}
