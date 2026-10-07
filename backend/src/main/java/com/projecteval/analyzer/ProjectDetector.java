package com.projecteval.analyzer;

import java.io.File;

public interface ProjectDetector {
    boolean canDetect(File projectDir);
    DetectionResult detect(File projectDir);
    String getDetectorName();
}
