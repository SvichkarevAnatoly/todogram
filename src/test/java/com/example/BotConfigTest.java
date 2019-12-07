package com.example;

import com.google.common.collect.ImmutableMap;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BotConfigTest {

    private static final ImmutableMap<String, String> envProps = ImmutableMap.of(
            "BOT_TOKEN", "someToken",
            "GIPHY_API_KEY", "someKey"
    );
    private final BotConfig config = ConfigFactory.create(BotConfig.class, envProps);

    @Test
    void giphyApiKey() {
        assertEquals("someKey", config.giphyApiKey());
    }

    @Test
    void botToken() {
        assertEquals("someToken", config.botToken());
    }
}