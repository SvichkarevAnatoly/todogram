package com.example.task;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Зарезервированные значения состояний задач
 */
public enum TaskStatus {
    @JsonProperty("pending")
    PENDING,
    @JsonProperty("deleted")
    DELETED,
    @JsonProperty("completed")
    COMPLETED,
    @JsonProperty("waiting")
    WAITING,
    @JsonProperty("recurring")
    RECURRING
}
