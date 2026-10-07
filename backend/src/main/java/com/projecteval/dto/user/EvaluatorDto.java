package com.projecteval.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatorDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String employeeCode;
    private String fullName;
    private String department;
    private boolean active;
    private LocalDateTime createdAt;
    private long assignedCount;
    private long completedCount;
}
