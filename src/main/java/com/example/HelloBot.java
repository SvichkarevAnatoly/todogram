package com.example;

import org.aeonbits.owner.ConfigCache;
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

public class HelloBot extends AbilityBot {

    // TODO: 07.12.2019 Превратить в бины
    private static final SecurityConfig securityConfig = ConfigCache.getOrCreate(SecurityConfig.class);
    private static final BotConfig botConfig = ConfigCache.getOrCreate(BotConfig.class);
    private final GifService gifService = new GifService();

    private static String BOT_NAME = "Никита";
    private static String BOT_TOKEN = securityConfig.botToken();

    private static String PROXY_HOST = botConfig.proxyHost();
    private static Integer PROXY_PORT = botConfig.proxyPort();
    private static String PROXY_USER = securityConfig.proxyUser();
    private static String PROXY_PASSWORD = securityConfig.proxyPassword();

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

    public void onUpdateReceived(Update update) {
        if (update.getMessage().getNewChatMembers().isEmpty()) {
            return;
        }

        final SendAnimation sendAnimation = new SendAnimation()
                .setChatId(update.getMessage().getChatId())
                .setAnimation(gifService.getGifUrl());
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
