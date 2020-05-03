package com.example;

import com.example.bot.CaptionService;
import com.example.bot.GifService;
import com.example.bot.InnerAbilityBot;
import com.example.bot.RequestService;
import com.example.bot.WelcomeBot;
import com.example.config.BotConfig;
import com.example.config.SecurityConfig;
import com.example.task.TaskService;
import com.example.task.TaskStorage;
import com.example.task.warrior.TaskWarriorService;
import org.aeonbits.owner.ConfigCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
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
    public TaskService taskService() {
        return new TaskService(new TaskWarriorService(), new TaskStorage());
    }

    @Bean
    public DBContext dbContext(SecurityConfig securityConfig) {
        return MapDBContext.onlineInstance(securityConfig.botName());
    }

    @Bean
    public InnerAbilityBot innerAbilityBot(WelcomeBot welcomeBot, TaskService taskService,
                                           SecurityConfig securityConfig,
                                           DBContext db, DefaultBotOptions botOptions) {
        return new InnerAbilityBot(
                welcomeBot,
                taskService,
                securityConfig.botToken(),
                securityConfig.botName(),
                db,
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
