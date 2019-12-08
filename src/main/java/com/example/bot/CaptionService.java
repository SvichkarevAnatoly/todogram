package com.example.bot;

import com.example.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class CaptionService {

    private final BotConfig config;
    private final RequestService requestService;

    @Autowired
    public CaptionService(BotConfig config, RequestService requestService) {
        this.config = config;
        this.requestService = requestService;
    }

    public String getCaption(String userName) {
        final String welcomeMessage = String.format(config.sloganWelcomeTemplate(), userName);

        final String json = requestService.getJson(config.sloganRequestUrlTemplate() + config.sloganQuery());
        final String slogan = requestService.getAttribute(json, "$.slogan.slogan");
        return welcomeMessage + slogan;
    }
}
