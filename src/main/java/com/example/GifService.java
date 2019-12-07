package com.example;

import com.jayway.jsonpath.JsonPath;
import org.aeonbits.owner.ConfigCache;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class GifService {

    // TODO: 07.12.2019 Превратить в бины
    private static final SecurityConfig securityConfig = ConfigCache.getOrCreate(SecurityConfig.class);
    private static final BotConfig botConfig = ConfigCache.getOrCreate(BotConfig.class);

    private static int gifOffset = botConfig.gifInitOffset();
    private static final String requestUrlTemplate = botConfig.gifRequestUrlTemplate();

    public String getGifUrl() {
        final String url = requestUrlTemplate + gifOffset + "&api_key=" + securityConfig.giphyApiKey();
        gifOffset++;

        final String json = getJson(url);
        return JsonPath.read(json, "$.data[0].images.fixed_height.url");
    }

    String getJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlObject.openStream()));
            String str = "";
            while (null != (str = br.readLine())) {
                json.append(str);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json.toString();
    }
}
