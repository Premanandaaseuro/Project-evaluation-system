package com.projecteval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjectEvalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectEvalApplication.class, args);
    }
}
