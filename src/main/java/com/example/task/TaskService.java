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

    public List<Task> getTasks() {
        if (taskStorage.isEmpty()) {
            updateStorage();
        }
        return taskStorage.getTasks();
    }

    public void createTask(Task task) {
        taskWarriorService.pushNewTask(task);
        updateStorage();
    }

    private void updateStorage() {
        taskStorage.load(taskWarriorService.fetchAllTasks());
    }
}
