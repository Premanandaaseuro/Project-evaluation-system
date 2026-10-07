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
public class PythonDetector implements ProjectDetector {

    private static final Logger log = LoggerFactory.getLogger(PythonDetector.class);

    @Override
    public boolean canDetect(File projectDir) {
        if (projectDir == null || !projectDir.exists()) return false;
        File req = new File(projectDir, "requirements.txt");
        File pyproject = new File(projectDir, "pyproject.toml");
        File managePy = new File(projectDir, "manage.py");
        return req.exists() || pyproject.exists() || managePy.exists();
    }

    @Override
    public DetectionResult detect(File projectDir) {
        DetectionResult result = DetectionResult.builder()
                .language("Python")
                .projectType("Python")
                .framework("Python")
                .buildTool("pip")
                .detectedPort(5000)
                .build();

        File req = new File(projectDir, "requirements.txt");
        File pyproject = new File(projectDir, "pyproject.toml");
        File managePy = new File(projectDir, "manage.py");

        String content = "";
        try {
            if (req.exists()) content += Files.readString(req.toPath());
            if (pyproject.exists()) content += Files.readString(pyproject.toPath());
        } catch (Exception e) {
            log.warn("Could not read python requirements: {}", e.getMessage());
        }

        if (managePy.exists() || content.toLowerCase().contains("django")) {
            result.setFramework("Django");
            result.setProjectType("Django");
            result.setDetectedPort(8000);
        } else if (content.toLowerCase().contains("flask")) {
            result.setFramework("Flask");
            result.setProjectType("Flask");
            result.setDetectedPort(5000);
        } else if (content.toLowerCase().contains("fastapi")) {
            result.setFramework("FastAPI");
            result.setProjectType("FastAPI");
            result.setDetectedPort(8000);
        }

        if (content.contains("psycopg2") || content.contains("asyncpg")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("PostgreSQL");
        } else if (content.contains("mysqlclient") || content.contains("pymysql")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("MySQL");
        } else if (content.contains("sqlite")) {
            result.setHasDatabaseConfig(true);
            result.setDatabaseType("SQLite");
        }

        if (content.contains("pytest") || new File(projectDir, "tests").exists()) {
            result.setHasTests(true);
            result.getDetectedFeatures().add("PyTest / Python Test Suite");
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

        List<String> endpoints = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(projectDir.toPath())) {
            paths.filter(p -> p.toString().endsWith(".py") && !p.toString().contains(".venv"))
                    .forEach(p -> {
                        try {
                            String code = Files.readString(p);
                            Pattern pattern = Pattern.compile("@(?:app|router)\\.(?:route|get|post|put|delete)\\s*\\(\\s*['\"]([^'\"]+)['\"]");
                            Matcher matcher = pattern.matcher(code);
                            while (matcher.find()) {
                                endpoints.add(matcher.group(1));
                            }
                        } catch (Exception ignored) {}
                    });
        } catch (Exception ignored) {}

        if (!endpoints.isEmpty()) {
            result.setApiEndpoints(endpoints);
        }

        return result;
    }

    @Override
    public String getDetectorName() {
        return "Python / Flask / Django Detector";
    }
}
