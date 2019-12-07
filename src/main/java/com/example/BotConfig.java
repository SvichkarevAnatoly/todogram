package com.example;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("system:env")
public interface BotConfig extends Config {

    @Key("GIPHY_API_KEY")
    String giphyApiKey();

    @Key("BOT_TOKEN")
    String botToken();
}
