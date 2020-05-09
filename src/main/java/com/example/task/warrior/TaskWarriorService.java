package com.example.task.warrior;

import com.example.task.Task;
import com.example.task.TaskParser;
import de.aaschmid.taskwarrior.client.MyTaskWarriorSslKeys;
import de.aaschmid.taskwarrior.client.MyTaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorAuthentication;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.example.task.TaskParser.parseJsonLines;
import static com.example.task.warrior.TaskWarriorParser.extractTasksJsonLines;
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
        final TaskwarriorConfiguration config = getConfig();
        final MyTaskWarriorSslKeys sslConfig = getSslConfig();
        final TaskwarriorMessage message = getSyncMessage(config);

        return new MyTaskwarriorClient(config, sslConfig).sendAndReceive(message);
    }

    private TaskwarriorMessage getSyncWithPayloadResponse(String payload) {
        final String payloadWithSyncKey = syncKey + "\n" + payload;

        final TaskwarriorConfiguration config = getConfig();
        final MyTaskWarriorSslKeys sslConfig = getSslConfig();
        final TaskwarriorMessage message = getSyncMessage(config, payloadWithSyncKey);

        return new MyTaskwarriorClient(config, sslConfig).sendAndReceive(message);
    }

    private TaskwarriorMessage getSyncMessage(TaskwarriorAuthentication config) {
        TaskwarriorRequestHeader header = getTaskwarriorRequestHeader(config);
        return taskwarriorMessage(header.toMap());
    }

    private TaskwarriorMessage getSyncMessage(TaskwarriorAuthentication config, String payload) {
        TaskwarriorRequestHeader header = getTaskwarriorRequestHeader(config);
        return taskwarriorMessage(header.toMap(), payload);
    }

    private TaskwarriorRequestHeader getTaskwarriorRequestHeader(TaskwarriorAuthentication config) {
        return taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
    }

    // TODO: Переписать на Owner
    private TaskwarriorConfiguration getConfig() {
        return new TaskwarriorConfiguration() {

            @Override
            public UUID getAuthKey() {
                String key = System.getenv("TASKWARRIOR_AUTH_KEY");
                try {
                    return UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getOrganization() {
                return System.getenv("TASKWARRIOR_AUTH_ORGANIZATION");
            }

            @Override
            public String getUser() {
                return System.getenv("TASKWARRIOR_AUTH_USER");
            }

            // Из-за приёмки файлов, а не строк - пришлось половину клиента скопировать
            @Override
            public File getCaCertFile() {
                return null;
            }

            @Override
            public File getPrivateKeyCertFile() {
                return null;
            }

            @Override
            public File getPrivateKeyFile() {
                return null;
            }

            @Override
            public InetAddress getServerHost() {
                String host = System.getenv("TASKWARRIOR_SERVER_HOST");
                try {
                    return InetAddress.getByName(host);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public int getServerPort() {
                String port = System.getenv("TASKWARRIOR_SERVER_PORT");
                try {
                    return Integer.decode(port);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        };
    }

    private MyTaskWarriorSslKeys getSslConfig() {
        return new MyTaskWarriorSslKeys() {
            @Override
            public String getCaCert() {
                final String base64Value = System.getenv("TASKWARRIOR_SSL_CERT_CA");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                return new String(decodedBytes);
            }

            @Override
            public String getPrivateKeyCert() {
                final String base64Value = System.getenv("TASKWARRIOR_SSL_CERT_KEY");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                return new String(decodedBytes);
            }

            @Override
            public String getPrivateKey() {
                final String base64Value = System.getenv("TASKWARRIOR_SSL_PRIVATE_KEY");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                return new String(decodedBytes);
            }
        };
    }
}
