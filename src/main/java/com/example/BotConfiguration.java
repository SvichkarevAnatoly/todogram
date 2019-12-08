package com.example;

import org.aeonbits.owner.ConfigCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Configuration
public class BotConfiguration {

    @Bean
    public GifService gifService() {
        return new GifService();
    }

    @Bean
    public SecurityConfig securityConfig() {
        return ConfigCache.getOrCreate(SecurityConfig.class);
    }

    @Bean
    public BotConfig botConfig() {
        return ConfigCache.getOrCreate(BotConfig.class);
    }

    @Bean
    public DefaultBotOptions botOptions(SecurityConfig securityConfig, BotConfig botConfig) {
        // TODO: Подумать куда вынести этот код в отдельное место
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        securityConfig.proxyUser(),
                        securityConfig.proxyPassword().toCharArray());
            }
        });
        ApiContextInitializer.init();

        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
        botOptions.setProxyHost(botConfig.proxyHost());
        botOptions.setProxyPort(botConfig.proxyPort());
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        return botOptions;
    }

    @Bean
    public HelloBot helloBot(SecurityConfig securityConfig, DefaultBotOptions botOptions) {
        final String BOT_TOKEN = securityConfig.botToken();
        return new HelloBot(BOT_TOKEN, "Никита", botOptions);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(HelloBot helloBot) {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(helloBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return botsApi;
    }
}
