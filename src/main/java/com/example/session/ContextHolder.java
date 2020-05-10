package com.example.session;

import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Хранитель контекста события
 */
public class ContextHolder {

    private MessageContext context;

    public void setContext(MessageContext context) {
        this.context = context;
    }

    public Update getUpdate() {
        return context.update();
    }

    public Long getChatId() {
        return context.chatId();
    }

    public User getUser() {
        return context.user();
    }
}
