package com.example.task.warrior;

public class TaskWarriorParser {

    public static String extractSyncKey(String response) {
        final String[] lines = response.split("\n");
        return lines[lines.length - 1];
    }

    public static String extractTasksJsonLines(String response) {
        final int lastNewLineIndex = response.lastIndexOf("\n");
        return new StringBuilder(response).delete(lastNewLineIndex, response.length()).toString();
    }
}
