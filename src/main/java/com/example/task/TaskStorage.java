package com.example.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class TaskStorage {

    private final Map<String, Task> tasks = new HashMap<>();

    public void load(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.uuid == null) {
                // TODO: решить что с такими задачами делать
                continue;
            }
            this.tasks.put(task.uuid, task);
        }
    }

    /**
     * Обновить задачу, если уже существовала - тогда true
     *
     * @param task
     * @return
     */
    public boolean update(Task task) {
        return tasks.replace(task.uuid, task) != null;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Получить список рабочих задач
     *
     * @return
     */
    public List<Task> getPendingTasks() {
        return tasks.values().stream()
                .filter(task -> "pending".equals(task.status))
                .collect(toList());
    }

    public List<Task> getPriorityPendingTasks(String priority) {
        return tasks.values().stream()
                .filter(task -> "pending".equals(task.status)
                        && priority.equals(task.priority))
                .collect(toList());
    }

    /**
     * Получить задачу по uuid
     */
    public Task getTaskByUuid(String uuid) {
        return tasks.get(uuid);
    }

    /**
     * Получить список сделанных задач
     *
     * @return
     */
    public List<Task> getCompletedTasks() {
        return tasks.values().stream()
                .filter(task -> "completed".equals(task.status))
                .collect(toList());
    }

    public List<Task> getDeletedTasks() {
        return tasks.values().stream()
                .filter(task -> "deleted".equals(task.status))
                .collect(toList());
    }
}
