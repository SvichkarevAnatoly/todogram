package com.example.statemachine;

import com.example.session.ContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static java.util.Arrays.asList;

public class ShowMainKeyboard implements Action<States, Events> {

    @Autowired
    public ContextHolder contextHolder;

    @Override
    public void execute(StateContext<States, Events> context) {
        showMainKeyboard();
    }

    private void showMainKeyboard() {
        final ReplyKeyboardMarkup keyboard = createKeyboard();
        SendMessage message = new SendMessage()
                .setChatId(contextHolder.getUpdate().getMessage().getChatId())
                // TODO: Понять можно ли показать клавиатуру без текстовки
                .setText("Добро пожаловать!")
                .setReplyMarkup(keyboard);
        try {
            contextHolder.getSender().execute(message);
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
}
