package com.example.bot;

import com.example.config.BotConfig;
import com.example.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class GifService {

    private final SecurityConfig securityConfig;
    private final BotConfig botConfig;
    private final RequestService requestService;

    @Autowired
    public GifService(SecurityConfig securityConfig, BotConfig botConfig,
                      RequestService requestService) {
        this.securityConfig = securityConfig;
        this.botConfig = botConfig;
        this.requestService = requestService;
    }

    public String getGifUrl() {
        final String url = botConfig.gifRequestUrlTemplate() + botConfig.gifOffset() +
                "&api_key=" + securityConfig.giphyApiKey();

        final String json = requestService.getJson(url);
        return requestService.getAttribute(json, "$.data[0].images.fixed_height.url");
    }
}
