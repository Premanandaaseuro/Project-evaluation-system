package com.projecteval;

import com.projecteval.analyzer.SecurityAnalyzer;
import com.projecteval.model.AutomatedTestResult;
import com.projecteval.model.TestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityAnalyzerTest {

    @Test
    @DisplayName("Should pass security checks when no hardcoded secrets exist")
    void testSecurityAnalyzerCleanDirectory() {
        SecurityAnalyzer analyzer = new SecurityAnalyzer();
        List<AutomatedTestResult> results = analyzer.analyzeSecurity(new File("."));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getCategory().equals("Security")));
    }
}
