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
    public WelcomeBot welcomeBot(SecurityConfig securityConfig, DefaultBotOptions botOptions) {
        return new WelcomeBot(
                securityConfig.botToken(),
                securityConfig.botName(),
                botOptions);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(WelcomeBot welcomeBot) {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(welcomeBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return botsApi;
    }
}
