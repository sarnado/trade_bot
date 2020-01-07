package com.currtex.tradebot.exchangeapi.json;

import com.google.gson.Gson;

public class LoginResponseDecoder {

    private LoginStructure responseStruct;

    public void decode(String json) {

        Gson g = new Gson();

        responseStruct = g.fromJson(json, LoginStructure.class);


    }

    public String getAuthToken(){

        return responseStruct.getToken();

    }




    public static class LoginStructure {
        private String token;
        private String status;

         String getToken() {
            return token;
        }

         String getStatus() {
            return status;
        }
    }
}

