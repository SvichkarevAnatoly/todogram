package com.example.task;

import com.example.task.warrior.TaskWarriorService;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.example.task.Task.DATE_TIME_FORMATTER;

public class TaskServiceImpl implements TaskService {

    private final TaskWarriorService taskWarriorService;
    private final TaskStorage taskStorage;

    public TaskServiceImpl(TaskWarriorService taskWarriorService, TaskStorage taskStorage) {
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

    public List<Task> getPriorityPendingTasks(String priority) {
        return taskStorage.getPriorityPendingTasks(priority);
    }

    public List<Task> getCompletedTasks() {
        return taskStorage.getCompletedTasks();
    }

    public List<Task> getDeletedTasks() {
        return taskStorage.getDeletedTasks();
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
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        originalTask.end = now.format(DATE_TIME_FORMATTER);
        originalTask.modified = now.format(DATE_TIME_FORMATTER);

        taskWarriorService.pushTask(originalTask);
        updateStorage();
    }

    public void setStatusDeleted(Task taskForDelete) {
        final Task originalTask = getTaskByUuid(taskForDelete.uuid);
        originalTask.status = "deleted";
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        originalTask.end = now.format(DATE_TIME_FORMATTER);
        originalTask.modified = now.format(DATE_TIME_FORMATTER);

        taskWarriorService.pushTask(originalTask);
        updateStorage();
    }

    public void changePriority(Task task, String priority) {
        final Task originalTask = getTaskByUuid(task.uuid);
        originalTask.priority = priority;
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        originalTask.modified = now.format(DATE_TIME_FORMATTER);

        taskWarriorService.pushTask(originalTask);
        updateStorage();
    }

    // TODO: Реализовать настоящий update с добавлением только инкремента
    private void updateStorage() {
        final List<Task> tasks = taskWarriorService.fetchAllTasks();
        taskStorage.load(tasks);
    }
}
