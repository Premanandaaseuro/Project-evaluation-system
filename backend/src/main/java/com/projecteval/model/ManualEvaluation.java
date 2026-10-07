package com.projecteval.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "manual_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Evaluator evaluator;

    @Column(name = "innovation_marks", nullable = false)
    private Double innovationMarks; // max 3

    @Column(name = "technical_marks", nullable = false)
    private Double technicalMarks; // max 4

    @Column(name = "documentation_marks", nullable = false)
    private Double documentationMarks; // max 3

    @Column(name = "presentation_marks", nullable = false)
    private Double presentationMarks; // max 2

    @Column(name = "outcome_marks", nullable = false)
    private Double outcomeMarks; // max 3

    @Column(name = "manual_total", nullable = false)
    private Double manualTotal; // max 15

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
