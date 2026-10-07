package com.projecteval.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class NodeDetector implements ProjectDetector {

    private static final Logger log = LoggerFactory.getLogger(NodeDetector.class);

    @Override
    public boolean canDetect(File projectDir) {
        if (projectDir == null || !projectDir.exists()) return false;
        File packageJson = new File(projectDir, "package.json");
        return packageJson.exists();
    }

    @Override
    public DetectionResult detect(File projectDir) {
        DetectionResult result = DetectionResult.builder()
                .language("JavaScript/TypeScript")
                .projectType("Node.js")
                .framework("Node.js")
                .buildTool("NPM")
                .detectedPort(3000)
                .build();

        File packageJson = new File(projectDir, "package.json");
        String content = "";
        try {
            content = Files.readString(packageJson.toPath());
        } catch (Exception e) {
            log.warn("Could not read package.json: {}", e.getMessage());
        }

        if (content.contains("\"react\"")) {
            result.setFramework("React");
            result.setProjectType("React");
            result.setDetectedPort(5173);
        } else if (content.contains("\"next\"")) {
            result.setFramework("Next.js");
            result.setProjectType("React/Next.js");
            result.setDetectedPort(3000);
        } else if (content.contains("\"express\"")) {
            result.setFramework("Express.js");
            result.setProjectType("Node.js/Express");
            result.setDetectedPort(3000);
        } else if (content.contains("\"@nestjs/core\"")) {
            result.setFramework("NestJS");
            result.setProjectType("NestJS");
            result.setDetectedPort(3000);
        }

        if (content.contains("\"typescript\"")) {
            result.setLanguage("TypeScript");
        }

        if (content.contains("\"pg\"") || content.contains("postgresql")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("PostgreSQL");
        } else if (content.contains("\"mysql\"") || content.contains("\"mysql2\"")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("MySQL");
        } else if (content.contains("\"mongoose\"") || content.contains("\"mongodb\"")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("MongoDB");
        }

        if (content.contains("\"test\"") || content.contains("\"playwright\"") || content.contains("\"jest\"") || content.contains("\"vitest\"")) {
            result.setHasTests(true);
            result.getDetectedFeatures().add("Automated Test Suite Configured");
        }

        File dockerfile = new File(projectDir, "Dockerfile");
        File dockerCompose = new File(projectDir, "docker-compose.yml");
        if (dockerfile.exists() || dockerCompose.exists()) {
            result.setHasDocker(true);
        }

        File readme = new File(projectDir, "README.md");
        if (readme.exists()) {
            result.setHasReadme(true);
        }

        // Scan Express/Route endpoints
        List<String> endpoints = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(projectDir.toPath())) {
            paths.filter(p -> (p.toString().endsWith(".js") || p.toString().endsWith(".ts")) && !p.toString().contains("node_modules"))
                    .forEach(p -> {
                        try {
                            String code = Files.readString(p);
                            Pattern pattern = Pattern.compile("(app|router)\\.(get|post|put|delete|patch)\\s*\\(\\s*['\"]([^'\"]+)['\"]");
                            Matcher matcher = pattern.matcher(code);
                            while (matcher.find()) {
                                endpoints.add(matcher.group(2).toUpperCase() + " " + matcher.group(3));
                            }
                        } catch (Exception ignored) {}
                    });
        } catch (Exception ignored) {}

        if (!endpoints.isEmpty()) {
            result.setApiEndpoints(endpoints);
            result.getDetectedFeatures().add("Detected " + endpoints.size() + " API endpoints");
        }

        return result;
    }

    @Override
    public String getDetectorName() {
        return "Node.js / React Detector";
    }
}
