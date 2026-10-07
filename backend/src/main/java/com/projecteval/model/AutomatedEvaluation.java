package com.projecteval.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "automated_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomatedEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "build_status", length = 30)
    private String buildStatus;

    @Column(name = "startup_status", length = 30)
    private String startupStatus;

    @Column(name = "api_status", length = 30)
    private String apiStatus;

    @Column(name = "ui_status", length = 30)
    private String uiStatus;

    @Column(name = "database_status", length = 30)
    private String databaseStatus;

    @Column(name = "security_status", length = 30)
    private String securityStatus;

    @Column(name = "code_quality_status", length = 30)
    private String codeQualityStatus;

    @Column(name = "documentation_status", length = 30)
    private String documentationStatus;

    @Column(name = "automated_score")
    private Double automatedScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", length = 30)
    @Builder.Default
    private EvaluationJobStatus jobStatus = EvaluationJobStatus.QUEUED;

    @Column(name = "build_logs", columnDefinition = "TEXT")
    private String buildLogs;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "automatedEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AutomatedTestResult> testResults = new ArrayList<>();
}
