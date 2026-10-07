package com.projecteval.service;

import com.projecteval.dto.auth.AuthResponse;
import com.projecteval.dto.auth.LoginRequest;
import com.projecteval.dto.auth.RegisterRequest;
import com.projecteval.exception.BadRequestException;
import com.projecteval.model.Evaluator;
import com.projecteval.model.Role;
import com.projecteval.model.Student;
import com.projecteval.model.User;
import com.projecteval.repository.EvaluatorRepository;
import com.projecteval.repository.StudentRepository;
import com.projecteval.repository.UserRepository;
import com.projecteval.security.JwtTokenProvider;
import com.projecteval.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EvaluatorRepository evaluatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String fullName = user.getUsername();
        if (user.getRole() == Role.ROLE_STUDENT) {
            fullName = studentRepository.findByUser(user)
                    .map(Student::getFullName)
                    .orElse(user.getUsername());
        } else if (user.getRole() == Role.ROLE_EVALUATOR) {
            fullName = evaluatorRepository.findByUser(user)
                    .map(Evaluator::getFullName)
                    .orElse(user.getUsername());
        }

        auditLogService.logAction(user.getId(), user.getUsername(), "LOGIN", "USER", user.getId(), "User logged in successfully");

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(fullName)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        Role role = Role.ROLE_STUDENT;
        if (registerRequest.getRole() != null) {
            try {
                role = Role.valueOf(registerRequest.getRole());
            } catch (IllegalArgumentException ignored) {
                if (registerRequest.getRole().equalsIgnoreCase("EVALUATOR")) {
                    role = Role.ROLE_EVALUATOR;
                } else if (registerRequest.getRole().equalsIgnoreCase("ADMIN")) {
                    role = Role.ROLE_ADMIN;
                }
            }
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String fullName = registerRequest.getFullName();
        if (role == Role.ROLE_STUDENT) {
            String studentCode = registerRequest.getStudentCode();
            if (studentCode == null || studentCode.isBlank()) {
                studentCode = "STU-" + System.currentTimeMillis() % 100000;
            }
            Student student = Student.builder()
                    .user(savedUser)
                    .fullName(fullName)
                    .studentCode(studentCode)
                    .department(registerRequest.getDepartment() != null ? registerRequest.getDepartment() : "General")
                    .semester(registerRequest.getSemester() != null ? registerRequest.getSemester() : 1)
                    .phone(registerRequest.getPhone())
                    .build();
            studentRepository.save(student);
        } else if (role == Role.ROLE_EVALUATOR) {
            String empCode = registerRequest.getEmployeeCode();
            if (empCode == null || empCode.isBlank()) {
                empCode = "EVAL-" + System.currentTimeMillis() % 100000;
            }
            Evaluator evaluator = Evaluator.builder()
                    .user(savedUser)
                    .fullName(fullName)
                    .employeeCode(empCode)
                    .department(registerRequest.getDepartment() != null ? registerRequest.getDepartment() : "General")
                    .build();
            evaluatorRepository.save(evaluator);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication);

        auditLogService.logAction(savedUser.getId(), savedUser.getUsername(), "REGISTER", "USER", savedUser.getId(), "User registered with role " + role.name());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .fullName(fullName)
                .build();
    }
}
