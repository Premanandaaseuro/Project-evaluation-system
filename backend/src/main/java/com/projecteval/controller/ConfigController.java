package com.projecteval.controller;

import com.projecteval.dto.common.ApiResponse;
import com.projecteval.dto.config.CriteriaDto;
import com.projecteval.exception.ResourceNotFoundException;
import com.projecteval.model.EvaluationCriteria;
import com.projecteval.repository.EvaluationCriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final EvaluationCriteriaRepository criteriaRepository;

    @GetMapping("/criteria")
    public ResponseEntity<ApiResponse<List<CriteriaDto>>> getCriteria() {
        List<EvaluationCriteria> list = criteriaRepository.findAll();
        List<CriteriaDto> dtos = list.stream().map(c -> CriteriaDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .maxMarks(c.getMaxMarks())
                .evaluationType(c.getEvaluationType())
                .criterionKey(c.getCriterionKey())
                .active(c.isActive())
                .build()).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PutMapping("/criteria/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CriteriaDto>> updateCriteria(
            @PathVariable Long id,
            @RequestBody CriteriaDto dto) {
        EvaluationCriteria criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criteria not found with id: " + id));

        if (dto.getName() != null) criteria.setName(dto.getName());
        if (dto.getDescription() != null) criteria.setDescription(dto.getDescription());
        if (dto.getMaxMarks() != null) criteria.setMaxMarks(dto.getMaxMarks());
        criteria.setActive(dto.isActive());

        EvaluationCriteria saved = criteriaRepository.save(criteria);
        return ResponseEntity.ok(ApiResponse.success(CriteriaDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .maxMarks(saved.getMaxMarks())
                .evaluationType(saved.getEvaluationType())
                .criterionKey(saved.getCriterionKey())
                .active(saved.isActive())
                .build()));
    }

    @GetMapping("/grading-scale")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGradingScale() {
        Map<String, String> scale = new HashMap<>();
        scale.put("90 - 100", "A+ (Outstanding)");
        scale.put("80 - 89.9", "A (Excellent)");
        scale.put("70 - 79.9", "B (Very Good)");
        scale.put("60 - 69.9", "C (Good / Satisfactory)");
        scale.put("50 - 59.9", "D (Pass)");
        scale.put("< 50", "Needs Improvement");
        return ResponseEntity.ok(ApiResponse.success(scale));
    }
}
