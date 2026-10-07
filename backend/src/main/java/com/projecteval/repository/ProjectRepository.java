package com.projecteval.repository;

import com.projecteval.model.Project;
import com.projecteval.model.ProjectStatus;
import com.projecteval.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStudent(Student student);
    List<Project> findByStudentId(Long studentId);
    List<Project> findByStatus(ProjectStatus status);
    long countByStatus(ProjectStatus status);
}
