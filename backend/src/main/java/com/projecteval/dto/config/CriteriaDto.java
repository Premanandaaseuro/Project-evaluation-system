package com.projecteval.dto.config;

import com.projecteval.model.EvaluationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaDto {
    private Long id;
    private String name;
    private String description;
    private Integer maxMarks;
    private EvaluationType evaluationType;
    private String criterionKey;
    private boolean active;
}
