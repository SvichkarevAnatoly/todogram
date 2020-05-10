package com.example.task;

import java.util.List;

public interface TaskService {

    List<Task> getAllTasks();

    List<Task> getPendingTasks();

    List<Task> getPriorityPendingTasks(String priority);

    List<Task> getCompletedTasks();

    List<Task> getDeletedTasks();

    Task getTaskByUuid(String uuid);

    void createTask(Task task);

    void setStatusCompleted(Task taskForDone);

    void setStatusDeleted(Task taskForDelete);

    void changePriority(Task task, String priority);
}
