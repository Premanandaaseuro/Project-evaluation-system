package com.projecteval.repository;

import com.projecteval.model.EvaluationCriteria;
import com.projecteval.model.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {
    List<EvaluationCriteria> findByActiveTrue();
    List<EvaluationCriteria> findByEvaluationTypeAndActiveTrue(EvaluationType evaluationType);
    Optional<EvaluationCriteria> findByCriterionKey(String criterionKey);
}
