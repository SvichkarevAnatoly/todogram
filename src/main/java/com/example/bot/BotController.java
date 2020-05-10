package com.example.bot;

import com.example.bot.ui.TelegramCommand;
import org.telegram.abilitybots.api.sender.MessageSender;

public interface BotController {

    /**
     * Запоминание sender для свободной отправки сообщений
     *
     * @param sender
     */
    void setSender(MessageSender sender);

    /**
     * Обработка сообщения
     */
    void action();

    /**
     * Обработка сообщения с командой
     *
     * @param command Команда
     */
    void action(TelegramCommand command);
}
