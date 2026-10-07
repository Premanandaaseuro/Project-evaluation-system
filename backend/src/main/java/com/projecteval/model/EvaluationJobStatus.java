package com.projecteval.model;

public enum EvaluationJobStatus {
    QUEUED,
    RUNNING,
    BUILDING,
    STARTING,
    TESTING,
    ANALYZING,
    COMPLETED,
    FAILED,
    TIMEOUT
}
