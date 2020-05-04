package com.example.task;

import com.example.task.warrior.TaskWarriorService;

import javax.annotation.PostConstruct;
import java.util.List;

public class TaskService {

    private final TaskWarriorService taskWarriorService;
    private final TaskStorage taskStorage;

    public TaskService(TaskWarriorService taskWarriorService, TaskStorage taskStorage) {
        this.taskWarriorService = taskWarriorService;
        this.taskStorage = taskStorage;
    }

    @PostConstruct
    public void init() {
        updateStorage();
    }

    public List<Task> getAllTasks() {
        return taskStorage.getAllTasks();
    }

    public List<Task> getPendingTasks() {
        return taskStorage.getPendingTasks();
    }

    public Task getTaskByUuid(String uuid) {
        return taskStorage.getTaskByUuid(uuid);
    }

    public void createTask(Task task) {
        taskWarriorService.pushTask(task);
        updateStorage();
    }

    public void setStatusCompleted(Task taskForDone) {
        final Task originalTask = getTaskByUuid(taskForDone.uuid);
        originalTask.status = "completed";

        taskWarriorService.pushTask(originalTask);
        updateStorage();
    }

    // TODO: Реализовать настоящий update с добавлением только инкремента
    private void updateStorage() {
        final List<Task> tasks = taskWarriorService.fetchAllTasks();
        taskStorage.load(tasks);
    }
}
