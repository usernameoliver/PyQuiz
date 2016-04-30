package com.example.hlt04.pyquiz.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by hlt04 on 4/30/16.
 */
public class HttpGet {
    public HttpGet() {
    }

    public String getJson(String urlstring) {
        String json = "";
        URL url = null;
        try {
            url = new URL(urlstring);
            URLConnection uc = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
            String line = "";
            while((line = in.readLine()) != null){
                json += line;
            }
            in.close();
        } catch (MalformedURLException e) {
            Log.d("MalformedURL", e.toString());

        } catch (IOException io) {
            Log.d("ioexception", io.toString());
        }

        Log.d("Albums JSON", "> " + json);

        // Check your log cat for JSON reponse
        Log.d("json length", "" + json.length());
        json = json.replace("function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); }", "hi");
        Log.d("json length", "" + json.length());
        return json;
    }
}
