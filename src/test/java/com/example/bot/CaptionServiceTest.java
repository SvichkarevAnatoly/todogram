package com.example.bot;

import com.example.config.BotConfig;
import org.aeonbits.owner.ConfigCache;
import org.junit.jupiter.api.Test;

class CaptionServiceTest {

    @Test
    void getCaption() {
        final BotConfig config = ConfigCache.getOrCreate(BotConfig.class);
        // final CaptionService captionService = new CaptionService(config, requestService);
        // System.out.println(captionService.getCaption(null));
    }
}