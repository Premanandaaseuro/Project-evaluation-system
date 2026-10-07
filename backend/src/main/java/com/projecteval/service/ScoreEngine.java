package com.projecteval.service;

import com.projecteval.model.ManualEvaluation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class ScoreEngine {

    public static final double MAX_AUTOMATED_SCORE = 85.0;
    public static final double MAX_MANUAL_SCORE = 15.0;
    public static final double MAX_TOTAL_SCORE = 100.0;

    // Default Maximums per Automated Category
    public static final double MAX_BUILD_STARTUP = 10.0;
    public static final double MAX_API = 20.0;
    public static final double MAX_UI = 15.0;
    public static final double MAX_DATABASE = 15.0;
    public static final double MAX_REQUIREMENTS = 15.0;
    public static final double MAX_SECURITY = 5.0;
    public static final double MAX_CODE_QUALITY = 3.0;
    public static final double MAX_DOCUMENTATION = 2.0;

    public double calculateAutomatedScore(Map<String, Double> categoryScores) {
        if (categoryScores == null || categoryScores.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        total += Math.min(categoryScores.getOrDefault("Build & Startup", 0.0), MAX_BUILD_STARTUP);
        total += Math.min(categoryScores.getOrDefault("API Testing", 0.0), MAX_API);
        total += Math.min(categoryScores.getOrDefault("UI Testing", 0.0), MAX_UI);
        total += Math.min(categoryScores.getOrDefault("Database & Integration", 0.0), MAX_DATABASE);
        total += Math.min(categoryScores.getOrDefault("Requirements Implementation", 0.0), MAX_REQUIREMENTS);
        total += Math.min(categoryScores.getOrDefault("Security", 0.0), MAX_SECURITY);
        total += Math.min(categoryScores.getOrDefault("Code Quality", 0.0), MAX_CODE_QUALITY);
        total += Math.min(categoryScores.getOrDefault("Documentation", 0.0), MAX_DOCUMENTATION);

        return round(Math.min(total, MAX_AUTOMATED_SCORE));
    }

    public double calculateManualScore(ManualEvaluation manual) {
        if (manual == null) {
            return 0.0;
        }
        double total = 0.0;
        if (manual.getInnovationMarks() != null) total += Math.min(manual.getInnovationMarks(), 3.0);
        if (manual.getTechnicalMarks() != null) total += Math.min(manual.getTechnicalMarks(), 4.0);
        if (manual.getDocumentationMarks() != null) total += Math.min(manual.getDocumentationMarks(), 3.0);
        if (manual.getPresentationMarks() != null) total += Math.min(manual.getPresentationMarks(), 2.0);
        if (manual.getOutcomeMarks() != null) total += Math.min(manual.getOutcomeMarks(), 3.0);

        return round(Math.min(total, MAX_MANUAL_SCORE));
    }

    public double calculateFinalScore(Double automatedScore, Double manualScore) {
        double auto = automatedScore != null ? automatedScore : 0.0;
        double man = manualScore != null ? manualScore : 0.0;
        return round(Math.min(auto + man, MAX_TOTAL_SCORE));
    }

    public String calculateGrade(double finalScore) {
        if (finalScore >= 90.0) {
            return "A+";
        } else if (finalScore >= 80.0) {
            return "A";
        } else if (finalScore >= 70.0) {
            return "B";
        } else if (finalScore >= 60.0) {
            return "C";
        } else if (finalScore >= 50.0) {
            return "D";
        } else {
            return "Needs Improvement";
        }
    }

    public String generateRemarks(double finalScore, String grade) {
        if (finalScore >= 90.0) {
            return "Outstanding performance! Excellent technical architecture, code quality, and implementation.";
        } else if (finalScore >= 80.0) {
            return "Very good work. Meets all critical technical specifications and requirements effectively.";
        } else if (finalScore >= 70.0) {
            return "Good progress. Core functionality operates as expected with minor areas for improvement.";
        } else if (finalScore >= 60.0) {
            return "Satisfactory. Meets basic criteria but requires attention to edge cases and documentation.";
        } else if (finalScore >= 50.0) {
            return "Passable. Several components require refinement, additional testing, or bug fixes.";
        } else {
            return "Significant revisions required. Project fails key automated checks or essential deliverables.";
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
