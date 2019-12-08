package com.example.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Бизнес логика бота
 */
public class WelcomeBot {

    private final GifService gifService;
    private final CaptionService captionService;

    private InnerAbilityBot innerAbilityBot;

    public WelcomeBot(GifService gifService, CaptionService captionService) {
        this.gifService = gifService;
        this.captionService = captionService;
    }

    @Autowired
    public void setInnerAbilityBot(InnerAbilityBot innerAbilityBot) {
        // TODO: Чтобы не было циклической зависимости
        this.innerAbilityBot = innerAbilityBot;
    }

    public void onNewChatMembers(Update update) {
        final String userName = getUserName(update);

        final SendAnimation animation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl())
                .setCaption(captionService.getCaption(userName));

        innerAbilityBot.sendAnimation(animation);
    }

    private String getUserName(Update update) {
        final User newUser = update.getMessage().getNewChatMembers().get(0);
        return newUser.getFirstName() + (newUser.getLastName() == null ?
                "" : " " + newUser.getLastName());
    }
}
