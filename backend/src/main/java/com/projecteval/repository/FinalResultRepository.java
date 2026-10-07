package com.projecteval.repository;

import com.projecteval.model.FinalResult;
import com.projecteval.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalResultRepository extends JpaRepository<FinalResult, Long> {
    Optional<FinalResult> findByProject(Project project);
    Optional<FinalResult> findByProjectId(Long projectId);
}
