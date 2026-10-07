package com.projecteval;

import com.projecteval.model.ManualEvaluation;
import com.projecteval.service.ScoreEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreEngineTest {

    private ScoreEngine scoreEngine;

    @BeforeEach
    void setUp() {
        scoreEngine = new ScoreEngine();
    }

    @Test
    @DisplayName("Should correctly compute automated score with rubric caps")
    void testCalculateAutomatedScore() {
        Map<String, Double> categoryScores = new HashMap<>();
        categoryScores.put("Build & Startup", 10.0);
        categoryScores.put("API Testing", 20.0);
        categoryScores.put("UI Testing", 15.0);
        categoryScores.put("Database & Integration", 15.0);
        categoryScores.put("Requirements Implementation", 15.0);
        categoryScores.put("Security", 5.0);
        categoryScores.put("Code Quality", 3.0);
        categoryScores.put("Documentation", 2.0);

        double total = scoreEngine.calculateAutomatedScore(categoryScores);
        assertEquals(85.0, total);
    }

    @Test
    @DisplayName("Should cap manual evaluation at exactly 15 marks")
    void testCalculateManualScore() {
        ManualEvaluation manual = ManualEvaluation.builder()
                .innovationMarks(3.0)
                .technicalMarks(4.0)
                .documentationMarks(3.0)
                .presentationMarks(2.0)
                .outcomeMarks(3.0)
                .build();

        double manualTotal = scoreEngine.calculateManualScore(manual);
        assertEquals(15.0, manualTotal);
    }

    @Test
    @DisplayName("Should correctly calculate letter grades based on ranges")
    void testGradeCalculations() {
        assertEquals("A+", scoreEngine.calculateGrade(95.0));
        assertEquals("A+", scoreEngine.calculateGrade(90.0));
        assertEquals("A", scoreEngine.calculateGrade(85.5));
        assertEquals("B", scoreEngine.calculateGrade(75.0));
        assertEquals("C", scoreEngine.calculateGrade(65.0));
        assertEquals("D", scoreEngine.calculateGrade(55.0));
        assertEquals("Needs Improvement", scoreEngine.calculateGrade(45.0));
    }
}
