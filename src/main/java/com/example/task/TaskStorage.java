package com.example.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.task.Task.DATE_TIME_FORMATTER;
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
                .filter(task -> task.status == TaskStatus.PENDING)
                .collect(toList());
    }

    public List<Task> getPriorityPendingTasks(TaskPriority priority) {
        return tasks.values().stream()
                .filter(task -> task.status == TaskStatus.PENDING)
                .filter(task -> priority.equals(task.priority))
                .sorted((task1, task2) ->
                        LocalDateTime.parse(task2.modified, DATE_TIME_FORMATTER)
                                .compareTo(LocalDateTime.parse(task1.modified, DATE_TIME_FORMATTER)))
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
                .filter(task -> task.status == TaskStatus.COMPLETED)
                // Чтобы не падали на некорректных данных
                .filter(task -> task.end != null)
                .sorted((task1, task2) ->
                        LocalDateTime.parse(task2.end, DATE_TIME_FORMATTER)
                                .compareTo(LocalDateTime.parse(task1.end, DATE_TIME_FORMATTER)))
                .collect(toList());
    }

    public List<Task> getDeletedTasks() {
        return tasks.values().stream()
                .filter(task -> TaskStatus.DELETED == task.status)
                // Чтобы не падали на некорректных данных
                .filter(task -> task.end != null)
                .sorted((task1, task2) ->
                        LocalDateTime.parse(task2.end, DATE_TIME_FORMATTER)
                                .compareTo(LocalDateTime.parse(task1.end, DATE_TIME_FORMATTER)))
                .collect(toList());
    }
}
