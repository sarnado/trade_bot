package com.currtex.tradebot.requestapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestExchange implements RequestApi {

    private String domain = "http://currtex.com";
    private String command;

    private String botKey = "052F8F886350D70B6FDA08130C5A30EAD480746F1A163CA556A2112D5021804E46C83474D2EB743CEC5E5D63C2FC9D5E610FFF1C77FF2733101FF866B02DD108";
    private String authToken = "";

    public void setAuthToken(String authToken) {
        this.authToken = "Bearer "+authToken;
    }


    public void setDomain(String domain) {
        this.domain = domain;
    }
    @Override
    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String executeRequest() {

        String url = domain+"/api/"+command;

        try{
            URL urlObj = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

            connection.setUseCaches(false);

            connection.setRequestMethod("GET");

            connection.setReadTimeout(100000);

            connection.setConnectTimeout(100000);

            connection.addRequestProperty("Bot-Key", botKey);

            if(!authToken.equals("")){
                connection.addRequestProperty("Authorization", authToken);
            }

            connection.connect();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String responseStr;

                while ((responseStr = in.readLine()) != null){

                    sb.append(responseStr);
                    sb.append('\n');

                }
                connection.disconnect();

                 //System.out.println(sb.toString());

                return sb.toString();

            }else if(connection.getResponseCode() == 401){
                return "Error_auth";
            } else{

                return "Connection_error";
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


}
