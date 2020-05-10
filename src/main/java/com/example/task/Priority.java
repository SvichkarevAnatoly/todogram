package com.example.task;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Зарезервированные значения приоритетов
 */
public enum Priority {
    @JsonProperty("H")
    TODAY,
    @JsonProperty("M")
    WEEK,
    @JsonProperty("L")
    LATER
}
