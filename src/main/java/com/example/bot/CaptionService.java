package com.example.bot;

import com.jayway.jsonpath.JsonPath;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class CaptionService {

    private static final String welcomeMessageTemplate = "Добро пожаловать, %s!\n\n";

    public String getCaption(Update update) {
        final User newUser = update.getMessage().getNewChatMembers().get(0);
        final String userName = newUser.getFirstName() + (newUser.getLastName() == null ? "" : " " + newUser.getLastName());
        final String welcomeMessage = String.format(welcomeMessageTemplate, userName);

        final String json = getJson("http://free-generator.ru/generator.php?action=slogan&category=35&name=%D0%9C%D0%BE%D0%BD%D0%B8%D1%82%D0%BE%D1%80%D0%B8%D0%BD%D0%B3");
        final String slogan = JsonPath.read(json, "$.slogan.slogan");
        return welcomeMessage + slogan;
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
