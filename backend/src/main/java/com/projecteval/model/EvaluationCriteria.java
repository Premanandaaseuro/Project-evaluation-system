package com.projecteval.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false, length = 30)
    private EvaluationType evaluationType;

    @Column(name = "criterion_key", length = 50)
    private String criterionKey;

    @Builder.Default
    private boolean active = true;
}
