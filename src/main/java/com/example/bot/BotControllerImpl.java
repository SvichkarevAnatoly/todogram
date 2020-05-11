package com.example.bot;


import com.example.project.ProjectService;
import com.example.session.ContextHolder;
import com.example.setting.SettingService;
import com.example.statemachine.Events;
import com.example.statemachine.StateMachineFactoryHelper;
import com.example.statemachine.States;
import com.example.task.Task;
import com.example.task.TaskPriority;
import com.example.task.TaskService;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class BotControllerImpl implements BotController {

    private MessageSender sender;
    private final ContextHolder context;

    private StateMachineFactoryHelper stateMachineFactoryHelper;

    private final TaskService taskService;
    private final ProjectService projectService;
    private final SettingService settingService;

    public BotControllerImpl(
            ContextHolder context,
            TaskService taskService, ProjectService projectService, SettingService settingService) {

        this.context = context;
        this.taskService = taskService;
        this.projectService = projectService;
        this.settingService = settingService;
    }

    @Autowired
    public void setStateMachineFactoryHelper(StateMachineFactoryHelper stateMachineFactoryHelper) {
        this.stateMachineFactoryHelper = stateMachineFactoryHelper;
    }

    @Override
    public void action() {
        final String userName = context.getUser().getUserName();
        final StateMachine<States, Events> stateMachine = stateMachineFactoryHelper.getStateMachine(userName);

        final States currentState = stateMachine.getState().getId();
        stateMachine.getTransitions().stream()
                .filter(transition -> transition.getSource().getId() == currentState)
                // TODO: Подумать нужен ли мне контекст и как бы его передать
                .filter(transition -> transition.transit(null))
                .findFirst()
                .ifPresent(transition -> stateMachine.sendEvent(transition.getTrigger().getEvent()));
        stateMachineFactoryHelper.saveState(stateMachine, userName);
    }

    private void router(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final String text = update.getMessage().getText();
            switch (text) {
                case "Сегодня":
                    listPendingTasks(update, TaskPriority.TODAY);
                    break;
                case "Неделя":
                    listPendingTasks(update, TaskPriority.WEEK);
                    break;
                case "Потом":
                    listPendingTasks(update, TaskPriority.LATER);
                    break;
                case "Завершенные":
                    listCompletedTasks(update);
                    break;
                case "Удалённые":
                    listDeletedTasks(update);
                    break;
                case "Проекты":
                    showProjects(update);
                    break;
                default:
                    createTask(update);
                    listPendingTasks(update, TaskPriority.TODAY);
                    break;
            }
        } else {
            if (update.hasCallbackQuery()) {
                parseCallbackQuery(update);
            }
        }
    }

    private void showProjects(Update update) {
        final List<String> projects = projectService.getProjects();
        final String text = String.join("\n", projects);
        sendText(text, update);
    }

    private void showKeyboard(Update update) {
        final ReplyKeyboardMarkup keyboard = createKeyboard();
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                // TODO: Понять можно ли показать клавиатуру без текстовки
                .setText("Добро пожаловать!")
                .setReplyMarkup(keyboard);
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listPendingTasks(Update update, TaskPriority priority) {
        final List<Task> tasks = taskService.getPriorityPendingTasks(priority);

        final SendMessage message;
        if (tasks.isEmpty()) {
            message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Задач нет");
        } else {
            final String text = IntStream.range(0, tasks.size())
                    .mapToObj(i -> (i + 1) + ") " + tasks.get(i).description)
                    .collect(Collectors.joining("\n"));

            message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(text)
                    .setReplyMarkup(new InlineKeyboardMarkup(singletonList(singletonList(
                            new InlineKeyboardButton("Подробнее")
                                    .setCallbackData("Подробнее " + priority)
                    ))));
        }

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listIndividualPendingTasks(Update update, TaskPriority priority) {
        sendEveryTask(update, taskService.getPriorityPendingTasks(priority));
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
        final String userName = update.getMessage().getFrom().getUserName();
        newTask.project = settingService.getDefaultProject(userName);
        taskService.createTask(newTask);

        sendText("Задача создана", update);
    }

    private void sendText(String text, Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup createKeyboard() {
        final KeyboardRow row1 = new KeyboardRow();
        final KeyboardRow row2 = new KeyboardRow();
        final KeyboardRow row3 = new KeyboardRow();
        row1.add("Сегодня");
        row1.add("Неделя");
        row1.add("Потом");
        row2.add("Завершенные");
        row2.add("Удалённые");
        row3.add("Проекты");
        return new ReplyKeyboardMarkup(asList(row1, row2, row3));
    }

    private void parseCallbackQuery(Update update) {
        final String data = update.getCallbackQuery().getData();
        if (data.equals("Подробнее H")) {
            listIndividualPendingTasks(update, TaskPriority.TODAY);
            deleteMessage(update);
        } else if (data.equals("Подробнее M")) {
            listIndividualPendingTasks(update, TaskPriority.WEEK);
            deleteMessage(update);
        } else if (data.equals("Подробнее L")) {
            listIndividualPendingTasks(update, TaskPriority.LATER);
            deleteMessage(update);
        } else if (data.startsWith("done ")) {
            final String taskUuid = data.substring("done ".length());
            final Task taskForDone = taskService.getTaskByUuid(taskUuid);
            taskService.setStatusCompleted(taskForDone);
            deleteMessage(update);
        } else if (data.startsWith("edit ")) {
            final String taskUuid = data.substring("edit ".length());
            showExtendedInlineKeyboard(update, taskUuid);
        } else if (data.startsWith("delete ")) {
            final String taskUuid = data.substring("delete ".length());
            final Task taskForDelete = taskService.getTaskByUuid(taskUuid);
            taskService.setStatusDeleted(taskForDelete);
            deleteMessage(update);
        } else if (data.startsWith("H ")) {
            final String taskUuid = data.substring("H ".length());
            final Task task = taskService.getTaskByUuid(taskUuid);
            taskService.changePriority(task, "H");
            deleteMessage(update);
        } else if (data.startsWith("M ")) {
            final String taskUuid = data.substring("M ".length());
            final Task task = taskService.getTaskByUuid(taskUuid);
            taskService.changePriority(task, "M");
            deleteMessage(update);
        } else if (data.startsWith("L ")) {
            final String taskUuid = data.substring("L ".length());
            final Task task = taskService.getTaskByUuid(taskUuid);
            taskService.changePriority(task, "L");
            deleteMessage(update);
        }
    }

    private void deleteMessage(Update update) {
        final Message message = update.getCallbackQuery().getMessage();
        final DeleteMessage deleteMessage = new DeleteMessage(
                message.getChatId(), message.getMessageId()
        );
        try {
            sender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendEveryTask(Update update, List<Task> tasks) {
        for (Task task : tasks) {
            String messageText = task.description;
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(task.uuid);

            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatId)
                    .setText(messageText)
                    .setReplyMarkup(inlineKeyboardMarkup);
            try {
                sender.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void showExtendedInlineKeyboard(Update update, String taskUuid) {
        final Message callbackMessage = update.getCallbackQuery().getMessage();
        final EditMessageReplyMarkup replyMarkup = new EditMessageReplyMarkup();
        replyMarkup.setChatId(callbackMessage.getChatId())
                .setMessageId(callbackMessage.getMessageId())
                .setReplyMarkup(new InlineKeyboardMarkup(asList(
                        getBasicEditRow(taskUuid),
                        asList(
                                new InlineKeyboardButton()
                                        .setText("Сегодня")
                                        .setCallbackData("H " + taskUuid),
                                new InlineKeyboardButton()
                                        .setText("Неделя")
                                        .setCallbackData("M " + taskUuid),
                                new InlineKeyboardButton()
                                        .setText("Потом")
                                        .setCallbackData("L " + taskUuid)
                        )
                )));

        try {
            sender.execute(replyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(String uuid) {
        return new InlineKeyboardMarkup(
                singletonList(getBasicEditRow(uuid))
        );
    }

    private List<InlineKeyboardButton> getBasicEditRow(String uuid) {
        return asList(
                new InlineKeyboardButton()
                        .setText(EmojiParser.parseToUnicode(":white_check_mark:"))
                        .setCallbackData("done " + uuid),
                new InlineKeyboardButton()
                        .setText(EmojiParser.parseToUnicode(":pencil:"))
                        .setCallbackData("edit " + uuid),
                new InlineKeyboardButton()
                        .setText(EmojiParser.parseToUnicode(":x:"))
                        .setCallbackData("delete " + uuid)
        );
    }
}
