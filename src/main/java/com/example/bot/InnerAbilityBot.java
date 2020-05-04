package com.example.bot;

import com.example.task.Task;
import com.example.task.TaskService;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Ability start() {
        return Ability.builder()
                .name("start")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> showKeyboard(ctx.update()))
                .post(ctx -> listPendingTasks(ctx.update()))
                .build();
    }

    public Ability common() {
        return Ability.builder()
                .name(DEFAULT)
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
                case "Текущие":
                    listPendingTasks(update);
                    break;
                case "Завершенные":
                    listCompletedTasks(update);
                    break;
                case "Удалённые":
                    listDeletedTasks(update);
                    break;
                default:
                    createTask(update);
                    listPendingTasks(update);
                    break;
            }
        } else {
            if (update.hasCallbackQuery()) {
                parseCallbackQuery(update);
            }
        }
    }

    private void showKeyboard(Update update) {
        final ReplyKeyboardMarkup keyboard = createKeyboard();
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                // TODO: Понять можно ли показать клавиатуру без текстовки
                .setText("Добро пожаловать!")
                .setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listPendingTasks(Update update) {
        sendEveryTask(update, taskService.getPendingTasks());
    }

    private void listCompletedTasks(Update update) {
        generateTasksList(update, taskService.getCompletedTasks());
    }

    private void listDeletedTasks(Update update) {
        generateTasksList(update, taskService.getDeletedTasks());
    }

    private void generateTasksList(Update update, List<Task> tasks) {
        final String text = IntStream.range(0, tasks.size())
                .mapToObj(i -> (i + 1) + ") " + tasks.get(i).description)
                .collect(Collectors.joining("\n"));
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

    private ReplyKeyboardMarkup createKeyboard() {
        final KeyboardRow row1 = new KeyboardRow();
        final KeyboardRow row2 = new KeyboardRow();
        row1.add("Текущие");
        row2.add("Завершенные");
        row2.add("Удалённые");
        return new ReplyKeyboardMarkup(asList(row1, row2));
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
        // Удалим это сообщение
        deleteMessage(update);
    }

    private void deleteMessage(Update update) {
        final Message message = update.getCallbackQuery().getMessage();
        final DeleteMessage deleteMessage = new DeleteMessage(
                message.getChatId(), message.getMessageId()
        );
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendEveryTask(Update update, List<Task> tasks) {
        for (Task task : tasks) {
            String messageText = task.description;
            long chatId = update.getMessage().getChatId();

            InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(task.uuid);

            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatId)
                    .setText(messageText)
                    .setReplyMarkup(inlineKeyboardMarkup);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

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
