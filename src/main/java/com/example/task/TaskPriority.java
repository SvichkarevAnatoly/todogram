package com.example.task;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Зарезервированные значения приоритетов задач
 */
public enum TaskPriority {
    @JsonProperty("H")
    TODAY,
    @JsonProperty("M")
    WEEK,
    @JsonProperty("L")
    LATER
}
