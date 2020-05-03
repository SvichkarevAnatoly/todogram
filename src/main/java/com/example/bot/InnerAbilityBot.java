package com.example.bot;

import com.example.task.Task;
import com.example.task.TaskService;
import com.vdurmont.emoji.EmojiParser;
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

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

/**
 * Имплементация телеграмм бота, отделённая от бизнес логики
 */
public class InnerAbilityBot extends AbilityBot {

    private WelcomeBot welcomeBot;
    private TaskService taskService;

    /**
     * Для тестирования дурацкой реализации telegram api
     */
    private Function<SendAnimation, SendAnimation> proxy;

    public InnerAbilityBot(WelcomeBot welcomeBot, TaskService
            taskService, String botToken, String botUsername, DBContext db, DefaultBotOptions botOptions) {
        super(botToken, botUsername, db, botOptions);
        this.welcomeBot = welcomeBot;
        this.taskService = taskService;
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
                .flag(update -> update.hasMessage() && update.getMessage().hasText())
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> makeResponse(ctx.update()))
                .build();
    }

    private Message makeResponse(Update update) {
        final Task newTask = new Task(update.getMessage().getText());
        taskService.createTask(newTask);

        sendEveryTask(update, taskService.getTasks());
        return null;
    }

    private void sendEveryTask(Update update, List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            String message_text = tasks.get(i).description;
            long chat_id = update.getMessage().getChatId();

            InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(i);

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

    @NotNull
    private InlineKeyboardMarkup createInlineKeyboard(int i) {
        return new InlineKeyboardMarkup(
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
