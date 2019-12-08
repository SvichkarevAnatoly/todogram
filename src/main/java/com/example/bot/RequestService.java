package com.example.bot;

import com.jayway.jsonpath.JsonPath;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class RequestService {

    public String getJson(String url) {
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

    public String getAttribute(String json, String jsonPath) {
        return JsonPath.read(json, jsonPath);
    }
}
