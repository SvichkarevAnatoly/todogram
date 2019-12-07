package com.example;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources("system:env")
public interface SecurityConfig extends Config {

    @Key("BOT_TOKEN")
    String botToken();

    @Key("GIPHY_API_KEY")
    String giphyApiKey();

    @Key("PROXY_USER")
    String proxyUser();

    @Key("PROXY_PASSWORD")
    String proxyPassword();
}
