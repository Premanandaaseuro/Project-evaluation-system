package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.user.EvaluatorDto;
import com.projecteval.dto.user.StudentDto;
import com.projecteval.exception.ResourceNotFoundException;
import com.projecteval.model.Evaluator;
import com.projecteval.model.Student;
import com.projecteval.model.User;
import com.projecteval.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final ProjectRepository projectRepository;
    private final EvaluatorAssignmentRepository assignmentRepository;
    private final ManualEvaluationRepository manualEvaluationRepository;

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<StudentDto> dtos = students.stream().map(s -> {
            long count = projectRepository.findByStudent(s).size();
            return StudentDto.builder()
                    .id(s.getId())
                    .userId(s.getUser().getId())
                    .username(s.getUser().getUsername())
                    .email(s.getUser().getEmail())
                    .studentCode(s.getStudentCode())
                    .fullName(s.getFullName())
                    .department(s.getDepartment())
                    .semester(s.getSemester())
                    .phone(s.getPhone())
                    .active(s.getUser().isActive())
                    .createdAt(s.getCreatedAt())
                    .projectCount(count)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/evaluators")
    public ResponseEntity<ApiResponse<List<EvaluatorDto>>> getAllEvaluators() {
        List<Evaluator> evaluators = evaluatorRepository.findAll();
        List<EvaluatorDto> dtos = evaluators.stream().map(e -> {
            long assigned = assignmentRepository.findByEvaluator(e).size();
            long completed = manualEvaluationRepository.findByEvaluator(e).size();
            return EvaluatorDto.builder()
                    .id(e.getId())
                    .userId(e.getUser().getId())
                    .username(e.getUser().getUsername())
                    .email(e.getUser().getEmail())
                    .employeeCode(e.getEmployeeCode())
                    .fullName(e.getFullName())
                    .department(e.getDepartment())
                    .active(e.getUser().isActive())
                    .createdAt(e.getCreatedAt())
                    .assignedCount(assigned)
                    .completedCount(completed)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(!user.isActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User active status updated", user.isActive()));
    }
}
