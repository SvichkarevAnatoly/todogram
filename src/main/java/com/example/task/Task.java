package com.example.task;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    public String description;
    public String entry;
    public String modified;
    public String status;
    public String uuid;
    public String end;

    public Task() {
    }

    public Task(String description) {
        this.description = description;
        this.status = "pending";
        this.uuid = UUID.randomUUID().toString();
    }
}
