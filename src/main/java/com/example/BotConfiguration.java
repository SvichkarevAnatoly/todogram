package com.example;

import com.example.bot.CaptionService;
import com.example.bot.GifService;
import com.example.bot.InnerAbilityBot;
import com.example.bot.RequestService;
import com.example.bot.WelcomeBot;
import com.example.config.BotConfig;
import com.example.config.SecurityConfig;
import org.aeonbits.owner.ConfigCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

// TODO: попробовать контекст создать на autowired, предварительно попробовав с тестами
@Configuration
public class BotConfiguration {

    @Bean
    public RequestService requestService() {
        return new RequestService();
    }

    @Bean
    public GifService gifService(SecurityConfig securityConfig, BotConfig botConfig, RequestService requestService) {
        return new GifService(securityConfig, botConfig, requestService);
    }

    @Bean
    public CaptionService captionService(BotConfig botConfig, RequestService requestService) {
        return new CaptionService(botConfig, requestService);
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
    public WelcomeBot welcomeBot(GifService gifService, CaptionService captionService) {
        return new WelcomeBot(gifService, captionService);
    }

    @Bean
    public InnerAbilityBot innerAbilityBot(SecurityConfig securityConfig, DefaultBotOptions botOptions) {
        return new InnerAbilityBot(
                securityConfig.botToken(),
                securityConfig.botName(),
                botOptions);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(AbilityBot innerAbilityBot) {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(innerAbilityBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return botsApi;
    }
}
