package com.example.task.warrior;

import com.example.task.Task;
import com.example.task.TaskParser;
import de.aaschmid.taskwarrior.client.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;

import java.net.URL;
import java.util.List;

import static com.example.task.TaskParser.parseJsonLines;
import static com.example.task.warrior.TaskWarriorParser.extractTasksJsonLines;
import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;

public class TaskWarriorService {

    private static String syncKey = "";

    public List<Task> fetchAllTasks() {
        final TaskwarriorMessage response = getSyncResponse();
        final String payload = response.getPayload().get();

        syncKey = TaskWarriorParser.extractSyncKey(payload);
        return parseJsonLines(extractTasksJsonLines(payload));
    }

    public void pushTask(Task task) {
        final String taskJson = TaskParser.toJson(task);
        final TaskwarriorMessage response = getSyncWithPayloadResponse(taskJson);
        syncKey = response.getPayload().get();
    }

    // TODO: Тоже нужно в отдельный класс вынести
    private TaskwarriorMessage getSyncResponse() {
        final TaskwarriorPropertiesConfiguration config = getConfig();
        final TaskwarriorMessage message = getSyncMessage(config);

        return new TaskwarriorClient(config).sendAndReceive(message);
    }

    private TaskwarriorMessage getSyncWithPayloadResponse(String payload) {
        final String payloadWithSyncKey = syncKey + "\n" + payload;

        final TaskwarriorPropertiesConfiguration config = getConfig();
        final TaskwarriorMessage message = getSyncMessage(config, payloadWithSyncKey);

        return new TaskwarriorClient(config).sendAndReceive(message);
    }

    private TaskwarriorMessage getSyncMessage(TaskwarriorPropertiesConfiguration config) {
        TaskwarriorRequestHeader header = getTaskwarriorRequestHeader(config);
        return taskwarriorMessage(header.toMap());
    }

    private TaskwarriorMessage getSyncMessage(TaskwarriorPropertiesConfiguration config, String payload) {
        TaskwarriorRequestHeader header = getTaskwarriorRequestHeader(config);
        return taskwarriorMessage(header.toMap(), payload);
    }

    private TaskwarriorRequestHeader getTaskwarriorRequestHeader(TaskwarriorPropertiesConfiguration config) {
        return taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
    }

    private TaskwarriorPropertiesConfiguration getConfig() {
        final URL resource = this.getClass().getResource("/taskwarrior.my.properties");
        return taskwarriorPropertiesConfiguration(resource);
    }
}
