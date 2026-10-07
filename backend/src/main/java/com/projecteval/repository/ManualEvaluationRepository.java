package com.projecteval.repository;

import com.projecteval.model.Evaluator;
import com.projecteval.model.ManualEvaluation;
import com.projecteval.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualEvaluationRepository extends JpaRepository<ManualEvaluation, Long> {
    List<ManualEvaluation> findByProject(Project project);
    Optional<ManualEvaluation> findByProjectId(Long projectId);
    List<ManualEvaluation> findByEvaluator(Evaluator evaluator);
    List<ManualEvaluation> findByEvaluatorId(Long evaluatorId);
}
