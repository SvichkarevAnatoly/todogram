package com.example.bot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

/**
 * Имплементация телеграмм бота, отделённая от бизнес логики
 */
public class InnerAbilityBot extends AbilityBot {

    private WelcomeBot welcomeBot;
    /**
     * Для тестирования дурацкой реализации telegram api
     */
    private Function<SendAnimation, SendAnimation> proxy;

    public InnerAbilityBot(WelcomeBot welcomeBot,
                           String botToken, String botUsername, DBContext db, DefaultBotOptions botOptions) {
        super(botToken, botUsername, db, botOptions);
        this.welcomeBot = welcomeBot;
    }

    // Для тестов
    public void setProxy(Function<SendAnimation, SendAnimation> proxy) {
        this.proxy = proxy;
    }

    void setSender(MessageSender sender) {
        this.sender = sender;
    }

    @Override
    public int creatorId() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public Ability sayWelcome() {
        return Ability.builder()
                .name(DEFAULT)
                .flag(update -> !update.getMessage().getNewChatMembers().isEmpty())
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> sendAnimation(welcomeBot.onNewChatMembers(ctx.update())))
                .build();
    }

    private Message sendAnimation(SendAnimation sendAnimation) {
        try {
            if (proxy == null) {
                return execute(sendAnimation);
            } else {
                proxy.apply(sendAnimation);
                return null;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
