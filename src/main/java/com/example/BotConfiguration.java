package com.example;

import com.example.bot.InnerAbilityBot;
import com.example.config.BotConfig;
import com.example.config.SecurityConfig;
import com.example.task.TaskService;
import com.example.task.TaskServiceImpl;
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
    public TaskService taskService() {
        return new TaskServiceImpl(new TaskWarriorService(), new TaskStorage());
    }

    @Bean
    public DBContext dbContext(SecurityConfig securityConfig) {
        return MapDBContext.onlineInstance(securityConfig.botName());
    }

    @Bean
    public InnerAbilityBot innerAbilityBot(TaskService taskService,
                                           SecurityConfig securityConfig,
                                           DBContext db, DefaultBotOptions botOptions) {
        return new InnerAbilityBot(
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
