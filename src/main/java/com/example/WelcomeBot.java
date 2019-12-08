package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Бизнес логика бота
 */
public class WelcomeBot {

    private final GifService gifService;
    private InnerAbilityBot innerAbilityBot;

    public WelcomeBot(GifService gifService) {
        this.gifService = gifService;
    }

    @Autowired
    public void setInnerAbilityBot(InnerAbilityBot innerAbilityBot) {
        // TODO: Чтобы не было циклической зависимости
        this.innerAbilityBot = innerAbilityBot;
    }

    public void onNewChatMembers(Update update) {
        final SendAnimation animation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl());
        innerAbilityBot.sendAnimation(animation);
    }
}
