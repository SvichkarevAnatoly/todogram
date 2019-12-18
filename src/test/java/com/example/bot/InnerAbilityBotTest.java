package com.example.bot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InnerAbilityBotTest {

    private static final int USER_ID = 1337;
    private static final long CHAT_ID = 1337L;

    // Your bot handle here
    private InnerAbilityBot bot;

    @Mock
    private WelcomeBot welcomeBot;

    // Your sender here
    @Mock
    private MessageSender sender;
    private DBContext db;

    @Mock
    private Function<SendAnimation, SendAnimation> proxy;

    @Captor
    private ArgumentCaptor<SendAnimation> sendAnimationCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Offline instance will get deleted at JVM shutdown
        db = MapDBContext.offlineInstance("test");

        // Create your bot
        bot = new InnerAbilityBot(null, null, db);
        // Create a new sender as a mock
        sender = mock(MessageSender.class);
        welcomeBot = mock(WelcomeBot.class);
        // Set your bot sender to the mocked sender
        // THIS is the line that prevents your bot from communicating with Telegram servers when it's running its own abilities
        // All method calls will go through the mocked interface -> which would do nothing except logging the fact that you've called this function with the specific arguments
        bot.setSender(sender);
        bot.setWelcomeBot(welcomeBot);
    }

    // We should clear the DB after every test as such
    @AfterEach
    public void tearDown() {
        db.clear();
    }

    @Test
    public void canSayHelloWorld() {
        Update update = new Update();

        User user = new User(USER_ID, "Abbas", false, "Abou Daya", "addo37", "en");
        // This is the context that you're used to, it is the necessary conumer item for the ability
        MessageContext context = MessageContext.newContext(update, user, CHAT_ID);

        final SendAnimation sendAnimation = new SendAnimation();
        sendAnimation.setChatId(CHAT_ID);
        sendAnimation.setAnimation("animation");

        when(welcomeBot.onNewChatMembers(any()))
                .thenReturn(sendAnimation);

        bot.setProxy(proxy);

        // We consume a context in the lambda declaration, so we pass the context to the action logic
        bot.sayWelcome().action()
                .accept(context);

        verify(proxy).apply(sendAnimationCaptor.capture());
        final SendAnimation actualSendAnimation = sendAnimationCaptor.getValue();
        assertEquals(actualSendAnimation.getChatId(), sendAnimation.getChatId());
        assertEquals(actualSendAnimation.getAnimation(), sendAnimation.getAnimation());
    }
}