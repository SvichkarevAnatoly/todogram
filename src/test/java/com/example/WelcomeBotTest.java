package com.example;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;

class WelcomeBotTest {
    @Test
    void name() {
        final String queryWelcomeInRussian = "%D0%B4%D0%BE%D0%B1%D1%80%D0%BE%20%D0%BF%D0%BE%D0%B6%D0%B0%D0%BB%D0%BE%D0%B2%D0%B0%D1%82%D1%8C";
        final String json = getJson("https://api.giphy.com/v1/gifs/search?" +
                "api_key=MDMQLQKA92JhXaGIbo2QFFZs5K4FdfD3&q=" +
                queryWelcomeInRussian + "&limit=1&offset=0&rating=G&lang=ru");

        String gifUrl = JsonPath.read(json, "$.data[0].images.fixed_height.url");
    }

    @Test
    void encode() throws UnsupportedEncodingException {
        assertEquals("%D0%B4%D0%BE%D0%B1%D1%80%D0%BE+%D0%BF%D0%BE%D0%B6%D0%B0%D0%BB%D0%BE%D0%B2%D0%B0%D1%82%D1%8C",
                URLEncoder.encode("добро пожаловать", "UTF-8"));
    }

    private String getJson(String url) {
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