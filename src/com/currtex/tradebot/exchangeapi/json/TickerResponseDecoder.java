package com.currtex.tradebot.exchangeapi.json;



import com.google.gson.Gson;


import java.util.Map;

public class TickerResponseDecoder {

    private TickerStructure tickerStructure;
    private String pairName;
    public TickerResponseDecoder(String pairName) {
        this.pairName = pairName;
    }

    public void decode(String json){

        Gson g = new Gson();

        tickerStructure = g.fromJson(json, TickerStructure.class);

    }
    public String getTickerPrice(){

        Map<String, TickerData> ticker = tickerStructure.getTicker();
        return ticker.get(pairName).getPrice();

    }






    private static class TickerData {

        private String price;

        String getPrice() {
            return price;
        }
    }

    private static class TickerStructure {
        private Map<String, TickerData> ticker;

        Map<String, TickerData> getTicker() {
            return ticker;
        }
    }
}

