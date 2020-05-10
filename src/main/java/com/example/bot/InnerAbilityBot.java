package com.example.bot;

import com.example.session.ContextHolder;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

/**
 * Имплементация телеграмм бота, отделённая от бизнес логики
 */
public class InnerAbilityBot extends AbilityBot {

    private final BotController controller;
    private final ContextHolder contextHolder;

    public InnerAbilityBot(BotController controller, ContextHolder contextHolder,
                           String botToken, String botUsername, DBContext db, DefaultBotOptions botOptions) {
        super(botToken, botUsername, db, botOptions);
        this.controller = controller;
        this.contextHolder = contextHolder;

        controller.setSender(sender);
    }

    @Override
    public int creatorId() {
        return 0;
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(this::action)
                // .post(ctx -> listPendingTasks(ctx.update(), "H"))
                .build();
    }

    public Ability common() {
        return Ability.builder()
                .name(DEFAULT)
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(this::action)
                .build();
    }

    private void action(MessageContext ctx) {
        contextHolder.setContext(ctx);
        controller.action();
        // showKeyboard(ctx.update());
    }
}
