package com.projecteval.repository;

import com.projecteval.model.AutomatedEvaluation;
import com.projecteval.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutomatedEvaluationRepository extends JpaRepository<AutomatedEvaluation, Long> {
    List<AutomatedEvaluation> findByProject(Project project);
    List<AutomatedEvaluation> findByProjectId(Long projectId);
    Optional<AutomatedEvaluation> findTopByProjectIdOrderByStartedAtDesc(Long projectId);
}
