package com.example.bot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: Восстановить
@Disabled
class InnerAbilityBotTest {

    private static final int USER_ID = 1337;
    private static final long CHAT_ID = 1337L;

    // Your bot handle here
    private InnerAbilityBot bot;

    // Your sender here
    @Mock
    private MessageSender sender;
    @Mock
    private DBContext db;

    @Mock
    private Function<SendAnimation, SendAnimation> proxy;

    @Captor
    private ArgumentCaptor<SendAnimation> sendAnimationCaptor;

    @Mock
    private Update update;

    @Mock
    private Message message;

    private final User user = new User(USER_ID, "Abbas", false, "Abou Daya", "addo37", "en");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Create your bot
        bot = null;//new InnerAbilityBot(null, null, null, null, null, db, new DefaultBotOptions());
        // Set your bot sender to the mocked sender
        // THIS is the line that prevents your bot from communicating with Telegram servers when it's running its own abilities
        // All method calls will go through the mocked interface -> which would do nothing except logging the fact that you've called this function with the specific arguments
        // bot.setSender(sender);
        // Прокси для перехвата итогового сообщения - спасибо "отличному" api
        // bot.setProxy(proxy);
    }

    // We should clear the DB after every test as such
    @AfterEach
    public void tearDown() {
        db.clear();
    }

    @Test
    public void canSayHelloWorld() {
        final SendAnimation sendAnimation = new SendAnimation();
        sendAnimation.setChatId(CHAT_ID);
        sendAnimation.setAnimation("animation");

        when(update.getMessage()).thenReturn(message);
        when(update.hasMessage()).thenReturn(true);
        when(message.getFrom()).thenReturn(user);
        when(message.getNewChatMembers()).thenReturn(singletonList(user));

        bot.onUpdatesReceived(singletonList(update));

        verify(proxy).apply(sendAnimationCaptor.capture());
        final SendAnimation actualSendAnimation = sendAnimationCaptor.getValue();
        assertEquals(actualSendAnimation.getChatId(), sendAnimation.getChatId());
        assertEquals(actualSendAnimation.getAnimation(), sendAnimation.getAnimation());
    }

    @Test
    @DisplayName("Любое сообщение, где нет новых пользователей игнорируется")
    void keepSilenceElse() {
        when(update.getMessage()).thenReturn(message);
        when(update.hasMessage()).thenReturn(true);
        when(message.getFrom()).thenReturn(user);

        // Новых пользователей нет
        when(message.getNewChatMembers()).thenReturn(emptyList());

        bot.onUpdatesReceived(singletonList(update));

        verify(proxy, never()).apply(any());
    }
}