package com.example;

import com.example.config.BotConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotConfigTest {

    private static BotConfig config = ConfigFactory.create(BotConfig.class);

    @Test
    void proxyHost() {
        assertThat(config.proxyHost())
                .isNotBlank();
    }

    @Test
    void proxyPort() {
        assertThat(config.proxyPort())
                .isGreaterThan(0);
    }
}