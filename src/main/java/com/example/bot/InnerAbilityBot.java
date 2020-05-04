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
import java.util.stream.Collectors;

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

    // TODO: разобраться с множеством разных ability
    @SuppressWarnings("unchecked")
    public Ability ability() {
        return Ability.builder()
                .name(DEFAULT)
                .flag(update -> true)
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> router(ctx.update()))
                .build();
    }

    private void router(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final String text = update.getMessage().getText();
            switch (text) {
                case "list":
                    listPendingTasks(update);
                    break;
                case "completed":
                    listCompletedTasks(update);
                    break;
                case "deleted":
                    listDeletedTasks(update);
                    break;
                default:
                    createTask(update);
            }
        } else {
            if (update.hasCallbackQuery()) {
                parseCallbackQuery(update);
            }
        }
    }

    private void listPendingTasks(Update update) {
        sendEveryTask(update, taskService.getPendingTasks());
    }

    private void listCompletedTasks(Update update) {
        final List<Task> completedTasks = taskService.getCompletedTasks();
        final String text = completedTasks.stream()
                .map(task -> task.description)
                .collect(Collectors.joining("\n\n"));
        sendText(text, update);
    }

    private void listDeletedTasks(Update update) {
        final List<Task> deletedTasks = taskService.getDeletedTasks();
        final String text = deletedTasks.stream()
                .map(task -> task.description)
                .collect(Collectors.joining("\n\n"));
        sendText(text, update);
    }

    private void createTask(Update update) {
        final Task newTask = new Task(update.getMessage().getText());
        taskService.createTask(newTask);

        sendText("Задача создана", update);
    }

    private void sendText(String text, Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void parseCallbackQuery(Update update) {
        final String data = update.getCallbackQuery().getData();
        if (data.startsWith("done ")) {
            final String taskUuid = data.substring("done ".length());
            final Task taskForDone = taskService.getTaskByUuid(taskUuid);
            taskService.setStatusCompleted(taskForDone);
        } else if (data.startsWith("delete ")) {
            final String taskUuid = data.substring("delete ".length());
            final Task taskForDelete = taskService.getTaskByUuid(taskUuid);
            taskService.setStatusDeleted(taskForDelete);
        }
    }

    private void sendEveryTask(Update update, List<Task> tasks) {
        for (Task task : tasks) {
            String message_text = task.description;
            long chat_id = update.getMessage().getChatId();

            InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(task.uuid);

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
    private InlineKeyboardMarkup createInlineKeyboard(String uuid) {
        return new InlineKeyboardMarkup(
                singletonList(asList(
                        new InlineKeyboardButton()
                                .setText(EmojiParser.parseToUnicode(":white_check_mark:"))
                                .setCallbackData("done " + uuid),
                        new InlineKeyboardButton()
                                .setText(EmojiParser.parseToUnicode(":pencil:"))
                                .setCallbackData("edit " + uuid),
                        new InlineKeyboardButton()
                                .setText(EmojiParser.parseToUnicode(":x:"))
                                .setCallbackData("delete " + uuid)
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
