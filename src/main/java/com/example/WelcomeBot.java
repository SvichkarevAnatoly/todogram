package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Бизнес логика бота
 */
public class WelcomeBot {

    private final GifService gifService;
    private AbilityBot abilityBot;

    public WelcomeBot(GifService gifService) {
        this.gifService = gifService;
    }

    @Autowired
    public void setAbilityBot(AbilityBot abilityBot) {
        // TODO: Чтобы не было циклической зависимости
        this.abilityBot = abilityBot;
    }

    public void onUpdateReceived(Update update) {
        if (update.getMessage().getNewChatMembers().isEmpty()) {
            return;
        }

        final SendAnimation sendAnimation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl());
        try {
            abilityBot.execute(sendAnimation); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
