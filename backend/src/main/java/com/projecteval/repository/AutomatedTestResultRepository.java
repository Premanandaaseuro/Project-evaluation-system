package com.projecteval.repository;

import com.projecteval.model.AutomatedEvaluation;
import com.projecteval.model.AutomatedTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomatedTestResultRepository extends JpaRepository<AutomatedTestResult, Long> {
    List<AutomatedTestResult> findByAutomatedEvaluation(AutomatedEvaluation evaluation);
    List<AutomatedTestResult> findByAutomatedEvaluationId(Long evaluationId);
    List<AutomatedTestResult> findByAutomatedEvaluationIdAndCategory(Long evaluationId, String category);
}
