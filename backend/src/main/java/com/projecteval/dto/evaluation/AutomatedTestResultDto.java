package com.projecteval.dto.evaluation;

import com.projecteval.model.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomatedTestResultDto {
    private Long id;
    private String testName;
    private String category;
    private String expectedResult;
    private String actualResult;
    private TestStatus status;
    private Long executionTime;
    private String errorMessage;
    private Double marksAwarded;
    private Double maxMarks;
}
