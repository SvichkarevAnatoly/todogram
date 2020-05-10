package com.example.task;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {

    /**
     * ISO 8601
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String description;
    public String entry;
    public String modified;
    public TaskStatus status;
    public String uuid;
    public String end;
    public TaskPriority priority;
    public String project;
    public Annotation[] annotations;

    public Task() {
    }

    public Task(String description) {
        this.description = description;
        this.status = TaskStatus.PENDING;
        this.uuid = UUID.randomUUID().toString();
        this.priority = TaskPriority.TODAY;

        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.entry = now.format(DATE_TIME_FORMATTER);
        this.modified = now.format(DATE_TIME_FORMATTER);
    }

    public static class Annotation {
        public String entry;
        public String description;
    }
}
