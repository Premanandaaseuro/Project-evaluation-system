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
public class JavaDetector implements ProjectDetector {

    private static final Logger log = LoggerFactory.getLogger(JavaDetector.class);

    @Override
    public boolean canDetect(File projectDir) {
        if (projectDir == null || !projectDir.exists()) return false;
        File pom = new File(projectDir, "pom.xml");
        File gradle = new File(projectDir, "build.gradle");
        File gradleKts = new File(projectDir, "build.gradle.kts");
        return pom.exists() || gradle.exists() || gradleKts.exists();
    }

    @Override
    public DetectionResult detect(File projectDir) {
        DetectionResult result = DetectionResult.builder()
                .language("Java")
                .projectType("Java")
                .framework("Standard Java")
                .detectedPort(8080)
                .build();

        File pom = new File(projectDir, "pom.xml");
        File gradle = new File(projectDir, "build.gradle");

        String buildContent = "";
        try {
            if (pom.exists()) {
                result.setBuildTool("Maven");
                buildContent = Files.readString(pom.toPath());
            } else if (gradle.exists()) {
                result.setBuildTool("Gradle");
                buildContent = Files.readString(gradle.toPath());
            }
        } catch (Exception e) {
            log.warn("Could not read build file: {}", e.getMessage());
        }

        if (buildContent.contains("spring-boot") || buildContent.contains("org.springframework")) {
            result.setFramework("Spring Boot");
            result.setProjectType("Spring Boot");
            result.getDetectedFeatures().add("REST API Architecture");
        }

        if (buildContent.contains("postgresql")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("PostgreSQL");
        } else if (buildContent.contains("mysql")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("MySQL");
        } else if (buildContent.contains("h2")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("H2");
        }

        // Check tests
        File testDir = new File(projectDir, "src/test/java");
        if (testDir.exists() && testDir.list() != null && testDir.list().length > 0) {
            result.setHasTests(true);
            result.getDetectedFeatures().add("Automated Unit/Integration Tests");
        }

        // Check Docker
        File dockerfile = new File(projectDir, "Dockerfile");
        File dockerCompose = new File(projectDir, "docker-compose.yml");
        if (dockerfile.exists() || dockerCompose.exists()) {
            result.setHasDocker(true);
            result.getDetectedFeatures().add("Containerized Architecture");
        }

        // Check Readme
        File readme = new File(projectDir, "README.md");
        if (readme.exists()) {
            result.setHasReadme(true);
        }

        // Scan controllers for endpoints
        List<String> endpoints = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(projectDir.toPath())) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            String code = Files.readString(p);
                            if (code.contains("@RestController") || code.contains("@Controller")) {
                                extractMappings(code, endpoints);
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

    private void extractMappings(String code, List<String> endpoints) {
        Pattern pattern = Pattern.compile("@(GetMapping|PostMapping|PutMapping|DeleteMapping|RequestMapping)\\s*\\(\\s*\"?([^\")]+)\"?\\s*\\)");
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String method = matcher.group(1).replace("Mapping", "").toUpperCase();
            if (method.equals("REQUEST")) method = "ANY";
            String path = matcher.group(2).replace("\"", "").replace("value=", "").trim();
            endpoints.add(method + " " + path);
        }
    }

    @Override
    public String getDetectorName() {
        return "Java / Spring Boot Detector";
    }
}
