package com.example.session;

import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Хранитель контекста события и отправителя
 */
public class ContextHolder {

    private MessageContext context;
    private MessageSender sender;

    public void setContext(MessageContext context) {
        this.context = context;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
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

    public MessageSender getSender() {
        return sender;
    }
}
