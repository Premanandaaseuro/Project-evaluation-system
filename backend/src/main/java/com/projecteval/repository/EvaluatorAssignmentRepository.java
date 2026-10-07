package com.projecteval.repository;

import com.projecteval.model.Evaluator;
import com.projecteval.model.EvaluatorAssignment;
import com.projecteval.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluatorAssignmentRepository extends JpaRepository<EvaluatorAssignment, Long> {
    List<EvaluatorAssignment> findByEvaluator(Evaluator evaluator);
    List<EvaluatorAssignment> findByEvaluatorId(Long evaluatorId);
    List<EvaluatorAssignment> findByProject(Project project);
    Optional<EvaluatorAssignment> findByProjectId(Long projectId);
    boolean existsByProjectIdAndEvaluatorId(Long projectId, Long evaluatorId);
}
