package com.projecteval.dto.evaluation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualEvaluationRequest {

    @NotNull(message = "Innovation marks are required")
    @DecimalMin(value = "0.0", message = "Innovation marks must be >= 0")
    @DecimalMax(value = "3.0", message = "Innovation marks cannot exceed 3.0")
    private Double innovationMarks;

    @NotNull(message = "Technical implementation marks are required")
    @DecimalMin(value = "0.0", message = "Technical marks must be >= 0")
    @DecimalMax(value = "4.0", message = "Technical marks cannot exceed 4.0")
    private Double technicalMarks;

    @NotNull(message = "Documentation marks are required")
    @DecimalMin(value = "0.0", message = "Documentation marks must be >= 0")
    @DecimalMax(value = "3.0", message = "Documentation marks cannot exceed 3.0")
    private Double documentationMarks;

    @NotNull(message = "Presentation marks are required")
    @DecimalMin(value = "0.0", message = "Presentation marks must be >= 0")
    @DecimalMax(value = "2.0", message = "Presentation marks cannot exceed 2.0")
    private Double presentationMarks;

    @NotNull(message = "Final outcome marks are required")
    @DecimalMin(value = "0.0", message = "Outcome marks must be >= 0")
    @DecimalMax(value = "3.0", message = "Outcome marks cannot exceed 3.0")
    private Double outcomeMarks;

    private String comments;
}
