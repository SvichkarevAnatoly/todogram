package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class WelcomeBot extends AbilityBot {

    private GifService gifService;

    public WelcomeBot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    @Autowired
    public void setGifService(GifService gifService) {
        this.gifService = gifService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage().getNewChatMembers().isEmpty()) {
            return;
        }

        final SendAnimation sendAnimation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl());
        try {
            execute(sendAnimation); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public int creatorId() {
        return 0;
    }
}
