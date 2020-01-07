package com.currtex.tradebot.requestapi;


import com.currtex.tradebot.logger.ILogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CmcRequestCreator implements RequestApi {



    private static String apiKey = "bd294e59-62da-4875-8c5c-b52eb4c26788";
    private String authToken = "";

    @Override
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private String command;
    private ILogger logger;
    private String params;

    public CmcRequestCreator(ILogger logger) {
        this.logger = logger;
    }

    public void setParams(){
        // Создание по ключу key, value
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String executeRequest(){
        String url = "https://pro-api.coinmarketcap.com/v1/" + command;

        try {


            URL obj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");

            connection.addRequestProperty("X-CMC_PRO_API_KEY", apiKey);

            connection.setUseCaches(false);

            connection.setConnectTimeout(2500);

            connection.setReadTimeout(2500);

            connection.connect();

            if(HttpsURLConnection.HTTP_OK == connection.getResponseCode())
            {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String responseLine;

                while((responseLine = in.readLine()) != null)
                {
                    sb.append(responseLine);
                    sb.append("\n");
                }

                logger.log("Coinmarketcap data received");

                connection.disconnect();

                return sb.toString();
            }else{

                logger.log("Error responce:" + connection.getResponseMessage());
                connection.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
