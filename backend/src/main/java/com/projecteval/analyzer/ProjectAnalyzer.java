package com.projecteval.analyzer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalyzer.class);
    private final List<ProjectDetector> detectors;

    public DetectionResult analyze(File projectDir) {
        if (projectDir == null || !projectDir.exists()) {
            return DetectionResult.builder()
                    .projectType("Unknown")
                    .language("Unknown")
                    .framework("Unknown")
                    .build();
        }

        for (ProjectDetector detector : detectors) {
            if (detector.canDetect(projectDir)) {
                log.info("Project detected by {}: {}", detector.getDetectorName(), projectDir.getAbsolutePath());
                return detector.detect(projectDir);
            }
        }

        // Generic fallback detector
        return DetectionResult.builder()
                .projectType("Generic Web Application")
                .language("Polyglot")
                .framework("Custom")
                .detectedPort(8080)
                .hasReadme(new File(projectDir, "README.md").exists())
                .hasDocker(new File(projectDir, "Dockerfile").exists())
                .build();
    }
}
