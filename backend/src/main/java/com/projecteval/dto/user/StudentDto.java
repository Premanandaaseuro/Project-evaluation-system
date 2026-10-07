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
public class StudentDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String studentCode;
    private String fullName;
    private String department;
    private Integer semester;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private long projectCount;
}
