package de.aaschmid.taskwarrior.client;

import de.aaschmid.taskwarrior.message.TaskwarriorMessage;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static de.aaschmid.taskwarrior.client.MyTaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.client.MyTaskwarriorMessageFactory.serialize;
import static java.util.Objects.requireNonNull;

// TODO: Если получится - можно выставить PR в оригинальный клиент на github

/**
 * Модификация оригинального клиента - чтобы можно было sslContext из строк создавать
 */
public class MyTaskwarriorClient {

    private final TaskwarriorServerLocation config;
    private final SSLContext sslContext;

    public MyTaskwarriorClient(TaskwarriorServerLocation config, SSLContext sslContext) {
        this.config = config;
        this.sslContext = sslContext;
    }

    public MyTaskwarriorClient(TaskwarriorServerLocation config,
                               MyTaskWarriorSslKeys sslKeys) {
        this.config = config;
        this.sslContext = MySslContextFactory.createSslContext(
                sslKeys.getCaCert(),
                sslKeys.getPrivateKeyCert(),
                sslKeys.getPrivateKey());
    }

    public MyTaskwarriorClient(TaskwarriorServerLocation config,
                               String caCert, String privateKeyCert, String privateKey) {
        this.config = config;
        this.sslContext = MySslContextFactory.createSslContext(caCert, privateKeyCert, privateKey);
    }

    public TaskwarriorMessage sendAndReceive(TaskwarriorMessage message) {
        requireNonNull(message, "'message' must not be null.");

        try (Socket socket = sslContext.getSocketFactory().createSocket(config.getServerHost(), config.getServerPort())) {
            return sendAndReceive(socket, message);
        } catch (IOException e) {
            throw new TaskwarriorClientException(
                    e,
                    "Could not create socket connection to '%s:%d'.",
                    config.getServerHost().getCanonicalHostName(),
                    config.getServerPort());
        }
    }

    private TaskwarriorMessage sendAndReceive(Socket socket, TaskwarriorMessage message) {
        try (OutputStream out = socket.getOutputStream(); InputStream in = socket.getInputStream()) {
            send(out, message);
            return receive(in);
        } catch (IOException e) {
            throw new TaskwarriorClientException("Could not open input and/or output stream of socket.", e);
        }
    }

    private void send(OutputStream out, TaskwarriorMessage message) {
        try {
            out.write(serialize(message));
            out.flush();
        } catch (IOException e) {
            throw new TaskwarriorClientException("Could not write and flush serialized message to output stream of socket.", e);
        }
    }

    private TaskwarriorMessage receive(InputStream in) {
        return deserialize(in);
    }
}
