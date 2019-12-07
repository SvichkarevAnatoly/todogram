package com.example;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;

public class HelloBot extends AbilityBot {

    private static String BOT_NAME = "Никита";
    private static String BOT_TOKEN = System.getenv().get("BOT_TOKEN");

    private static String PROXY_HOST = "en.socksy.seriyps.ru" /* proxy host */;
    private static Integer PROXY_PORT = 7777 /* proxy port */;
    private static String PROXY_USER = "tg-injectmocks" /* proxy user */;
    private static String PROXY_PASSWORD = "EcNenWpW" /* proxy password */;

    protected HelloBot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    public static void run() {
        try {

            // Create the Authenticator that will return auth's parameters for proxy authentication
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray());
                }
            });
            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Set up Http proxy
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            // Register your newly created AbilityBot
            HelloBot bot = new HelloBot(BOT_TOKEN, BOT_NAME, botOptions);

            botsApi.registerBot(bot);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public int creatorId() {
        return 0;
    }

    public static final List<String> gifLinks = asList(
            "https://media.giphy.com/media/l0MYC0LajbaPoEADu/giphy.gif",
            "https://media.giphy.com/media/l4JyOCNEfXvVYEqB2/giphy.gif",
            "https://media.giphy.com/media/Ae7SI3LoPYj8Q/giphy.gif",
            "https://media.giphy.com/media/FQyQEYd0KlYQ/giphy.gif",
            "https://media.giphy.com/media/OkJat1YNdoD3W/giphy.gif"
    );

    public void onUpdateReceived(Update update) {
        if (update.getMessage().getNewChatMembers().isEmpty()) {
            return;
        }

        final Random random = new Random();
        final int gifId = random.nextInt(gifLinks.size());
        final SendAnimation sendAnimation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifLinks.get(gifId));
        try {
            execute(sendAnimation); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }
}
