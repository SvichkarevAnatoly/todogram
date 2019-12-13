package com.example.config;

import org.aeonbits.owner.Config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

public interface BotConfig extends Config {

    default String proxyHost() {
        return "en.socksy.seriyps.ru";
    }

    default int proxyPort() {
        return 7777;
    }

    default int gifOffset() {
        return new Random().nextInt(100);
    }

    default String gifQuery() {
        try {
            return URLEncoder.encode("добро пожаловать", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException();
    }

    default String gifRequestUrlTemplate() {
        return "https://api.giphy.com/v1/gifs/search" +
                // TODO: Проверить как будет работать с аннотациями,
                //  если что можно вручную подставить
                "?q=" + gifQuery() +
                "&limit=1&rating=G&lang=ru&offset=";
    }

    default String sloganWelcomeTemplate() {
        return "Добро пожаловать, %s!\n\n";
    }

    default String sloganQuery() {
        return "Мониторинг";
    }

    default String sloganRequestUrlTemplate() {
        return "http://free-generator.ru/generator.php" +
                "?action=slogan&category=28&name=";
    }
}
