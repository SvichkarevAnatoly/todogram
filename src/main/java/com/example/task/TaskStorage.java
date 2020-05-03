package com.example.task;

import java.util.ArrayList;
import java.util.List;

public class TaskStorage {

    private List<Task> tasks = new ArrayList<>();

    public void load(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    // TODO: получить задачу по uuid
    // TODO: получить список рабочих задач
    // TODO: получить список сделанных задач
}
