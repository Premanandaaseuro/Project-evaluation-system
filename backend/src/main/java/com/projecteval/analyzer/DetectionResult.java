package com.projecteval.analyzer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResult {
    private String projectType;
    private String framework;
    private String buildTool;
    private String language;
    private Integer detectedPort;
    private boolean hasTests;
    private boolean hasDocker;
    private boolean hasReadme;
    private boolean hasDatabaseConfig;
    private String databaseType;
    @Builder.Default
    private List<String> dependencies = new ArrayList<>();
    @Builder.Default
    private List<String> detectedFeatures = new ArrayList<>();
    @Builder.Default
    private List<String> apiEndpoints = new ArrayList<>();
}
