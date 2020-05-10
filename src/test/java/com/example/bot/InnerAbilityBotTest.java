package com.example.bot;

import com.example.session.ContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class InnerAbilityBotTest {

    // Your bot handle here
    private InnerAbilityBot bot;
    private DBContext db;

    @Mock
    private BotController botController;
    @Mock
    private ContextHolder contextHolder;

    @Captor
    ArgumentCaptor<MessageContext> contextCaptor;

    private final User user = new User(
            1, "Abbas", false, "Abou Daya", "addo37", "en");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Offline instance will get deleted at JVM shutdown
        db = MapDBContext.offlineInstance("test");
        bot = new InnerAbilityBot(botController, contextHolder,
                null, null, db, new DefaultBotOptions());
    }

    // We should clear the DB after every test as such
    @AfterEach
    public void tearDown() {
        db.clear();
    }

    @Test
    @DisplayName("При старте контроллер получает sender")
    void setSender() {
        verify(botController).setSender(any());
    }

    @Test
    @DisplayName("При команде старт - контроллер получает этот аргумент")
    public void start() {
        final MessageContext messageContext = MessageContext.newContext(new Update(), user, 1L);
        bot.start().action().accept(messageContext);

        verify(contextHolder).setContext(contextCaptor.capture());
        assertEquals(messageContext, contextCaptor.getValue());

        verify(botController).action("start");
    }

    @Test
    @DisplayName("При любом сообщение вызывается action у контроллера")
    void common() {
        final MessageContext messageContext = MessageContext.newContext(new Update(), user, 1L);
        bot.general().action().accept(messageContext);

        verify(contextHolder).setContext(contextCaptor.capture());
        assertEquals(messageContext, contextCaptor.getValue());

        verify(botController).action();
    }
}