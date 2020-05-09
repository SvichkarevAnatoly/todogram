package com.example.config;

import org.aeonbits.owner.Config;

public interface BotConfig extends Config {

    default String proxyHost() {
        return "en.socksy.seriyps.ru";
    }

    default int proxyPort() {
        return 7777;
    }
}
