package com.example.statemachine.action;

import com.example.session.ContextHolder;
import com.example.statemachine.Events;
import com.example.statemachine.States;
import com.example.task.Task;
import com.example.task.TaskPriority;
import com.example.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.singletonList;

public class ShowTodayTasks implements Action<States, Events> {

    @Autowired
    public ContextHolder contextHolder;

    @Autowired
    public TaskService taskService;

    @Override
    public void execute(StateContext<States, Events> context) {
        listPendingTasks(contextHolder.getUpdate(), TaskPriority.TODAY);
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
            contextHolder.getSender().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
