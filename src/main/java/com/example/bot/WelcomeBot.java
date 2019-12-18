package com.example.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Бизнес логика бота
 */
public class WelcomeBot {

    private final GifService gifService;
    private final CaptionService captionService;

    public WelcomeBot(GifService gifService, CaptionService captionService) {
        this.gifService = gifService;
        this.captionService = captionService;
    }

    public SendAnimation onNewChatMembers(Update update) {
        final String userName = getUserName(update);

        return new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl())
                .setCaption(captionService.getCaption(userName));
    }

    private String getUserName(Update update) {
        final User newUser = update.getMessage().getNewChatMembers().get(0);
        return newUser.getFirstName() + (newUser.getLastName() == null ?
                "" : " " + newUser.getLastName());
    }
}
