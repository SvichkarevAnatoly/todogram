package com.example.bot;

import de.aaschmid.taskwarrior.client.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URL;
import java.util.function.Function;

import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

/**
 * Имплементация телеграмм бота, отделённая от бизнес логики
 */
public class InnerAbilityBot extends AbilityBot {

    private WelcomeBot welcomeBot;
    /**
     * Для тестирования дурацкой реализации telegram api
     */
    private Function<SendAnimation, SendAnimation> proxy;

    public InnerAbilityBot(WelcomeBot welcomeBot,
                           String botToken, String botUsername, DBContext db, DefaultBotOptions botOptions) {
        super(botToken, botUsername, db, botOptions);
        this.welcomeBot = welcomeBot;
    }

    // Для тестов
    public void setProxy(Function<SendAnimation, SendAnimation> proxy) {
        this.proxy = proxy;
    }

    void setSender(MessageSender sender) {
        this.sender = sender;
    }

    @Override
    public int creatorId() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public Ability sayWelcome() {
        return Ability.builder()
                .name(DEFAULT)
                .flag(update -> true)
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> makeResponse(ctx.update()))
                .build();
    }

    private Message makeResponse(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = getMessageFromTaskWarrior();
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(message_text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getMessageFromTaskWarrior() {
        final URL resource = this.getClass().getResource("/taskwarrior.my.properties");
        final TaskwarriorPropertiesConfiguration config = taskwarriorPropertiesConfiguration(resource);

        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = new TaskwarriorClient(config).sendAndReceive(message);
        return response.getPayload().orElse("Пустой ответ");
    }

    private Message sendAnimation(SendAnimation sendAnimation) {
        try {
            if (proxy == null) {
                return execute(sendAnimation);
            } else {
                proxy.apply(sendAnimation);
                return null;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
