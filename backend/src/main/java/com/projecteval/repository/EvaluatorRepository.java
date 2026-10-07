package com.projecteval.repository;

import com.projecteval.model.Evaluator;
import com.projecteval.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluatorRepository extends JpaRepository<Evaluator, Long> {
    Optional<Evaluator> findByUser(User user);
    Optional<Evaluator> findByUserId(Long userId);
    Optional<Evaluator> findByEmployeeCode(String employeeCode);
}
