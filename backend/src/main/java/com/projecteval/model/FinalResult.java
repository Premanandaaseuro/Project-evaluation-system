package com.projecteval.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    @JsonIgnore
    private Project project;

    @Column(name = "automated_score", nullable = false)
    private Double automatedScore;

    @Column(name = "manual_score", nullable = false)
    private Double manualScore;

    @Column(name = "final_score", nullable = false)
    private Double finalScore;

    @Column(length = 20, nullable = false)
    private String grade;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    private boolean published = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
