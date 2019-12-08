package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Имплементация телеграмм бота, отделённая от бизнес логики
 */
public class InnerAbilityBot extends AbilityBot {

    // TODO: Плохо, что inner знает о welcome bot, надо как-то подписать welcome на обновления
    private WelcomeBot welcomeBot;

    public InnerAbilityBot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    @Autowired
    public void setWelcomeBot(WelcomeBot welcomeBot) {
        this.welcomeBot = welcomeBot;
    }

    public int creatorId() {
        return 0;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage().getNewChatMembers().isEmpty()) {
            return;
        }

        welcomeBot.onNewChatMembers(update);
    }

    public Message sendAnimation(SendAnimation sendAnimation) {
        try {
            return execute(sendAnimation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
