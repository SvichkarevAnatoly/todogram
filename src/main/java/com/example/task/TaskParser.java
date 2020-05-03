package com.example.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Task parseJson(String taskJson) {
        try {
            return MAPPER.readValue(taskJson, Task.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Task task) {
        try {
            return MAPPER.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Task> parseJsonLines(String tasksJsonLines) {
        final List<Task> tasks = new ArrayList<>();
        for (String line : tasksJsonLines.split("\n")) {
            tasks.add(parseJson(line));
        }
        return tasks;
    }
}
