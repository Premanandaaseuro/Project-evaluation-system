package com.projecteval.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

@Service
public class DockerSandboxService {

    private static final Logger log = LoggerFactory.getLogger(DockerSandboxService.class);

    @Value("${app.sandbox.workspace-dir:./eval-workspace}")
    private String baseWorkspaceDir;

    @Value("${app.sandbox.timeout-seconds:120}")
    private int timeoutSeconds;

    public File createIsolatedWorkspace(Long projectId) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        File workspace = new File(baseWorkspaceDir, "project-" + projectId + "-" + uniqueId);
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        log.info("Created isolated workspace: {}", workspace.getAbsolutePath());
        return workspace;
    }

    public void cleanupWorkspace(File workspace) {
        if (workspace == null || !workspace.exists()) {
            return;
        }
        try {
            Files.walk(workspace.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            log.info("Cleaned up sandbox workspace: {}", workspace.getAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not completely delete workspace: {}", e.getMessage());
        }
    }

    public boolean isDockerAvailable() {
        try {
            Process process = new ProcessBuilder("docker", "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
