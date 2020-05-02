package com.example.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;
import de.aaschmid.taskwarrior.client.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorPropertiesConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;
import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
            final List<Task> tasks = getMessageFromTaskWarrior();
            for (int i = 0; i < tasks.size(); i++) {
                String message_text = tasks.get(i).description;
                long chat_id = update.getMessage().getChatId();

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
                        singletonList(asList(
                                new InlineKeyboardButton()
                                        .setText(EmojiParser.parseToUnicode(":white_check_mark:"))
                                        .setCallbackData("done " + i),
                                new InlineKeyboardButton()
                                        .setText(EmojiParser.parseToUnicode(":pencil:"))
                                        .setCallbackData("edit " + i),
                                new InlineKeyboardButton()
                                        .setText(EmojiParser.parseToUnicode(":x:"))
                                        .setCallbackData("delete " + i)
                        ))
                );

                SendMessage message = new SendMessage()
                        .setChatId(chat_id)
                        .setText(message_text)
                        .setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private List<Task> getMessageFromTaskWarrior() {
        final URL resource = this.getClass().getResource("/taskwarrior.my.properties");
        final TaskwarriorPropertiesConfiguration config = taskwarriorPropertiesConfiguration(resource);

        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder()
                .authentication(config)
                .type(TaskwarriorRequestHeader.MessageType.SYNC)
                .build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorMessage response = new TaskwarriorClient(config).sendAndReceive(message);
        final String tasksJson = getTasksInJson(response);
        List<Task> tasks = new ArrayList<>();
        try {
            tasks = asList(new ObjectMapper().readValue(tasksJson.getBytes(UTF_16), Task[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @NotNull
    private String getTasksInJson(TaskwarriorMessage response) {
        final String data = response.getPayload().orElse("");
        final int lastNewLineIndex = data.lastIndexOf("\n");
        final String tasksSemiJson = new StringBuilder(data).delete(lastNewLineIndex, data.length()).toString();

        return ("[" + tasksSemiJson + "]").replace("}\n{", "},\n{");
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
