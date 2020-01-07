package com.currtex.tradebot.exchangeapi;

import com.currtex.tradebot.exchangeapi.json.GroupsResponseDecoder;
import com.currtex.tradebot.exchangeapi.json.LoginResponseDecoder;
import com.currtex.tradebot.exchangeapi.json.TickerResponseDecoder;
import com.currtex.tradebot.logger.ILogger;
import com.currtex.tradebot.requestapi.RequestApi;

public class ExchangeApi {

    private String command;
    private RequestApi request;
    private ILogger logger;
    private String authToken;
    private String pairName;

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    public ExchangeApi(RequestApi rc, ILogger logger) {
        this.request = rc;
        this.logger = logger;
        authToken = login();
    }

    private String login(){

        String botLogin = "superBotFather";
        String botPass = "11471147aaA!";
        String command = "auth/login?name="+ botLogin +"&password="+ botPass;

        request.setCommand(command);

        String responseJson = request.executeRequest();

        if(responseJson.equals("Connection_error")){


            logger.log("Error connecting to the exchange API");

        }else{
            LoginResponseDecoder loginResponse = new LoginResponseDecoder();

            loginResponse.decode(responseJson);

            logger.log("Bot logged by nick: "+ botLogin);

            return loginResponse.getAuthToken();


        }
        return null;

    }
    public String addLimitOrder(String type, String price, String quantity){

        request.setAuthToken(authToken);
        request.setCommand("order/create?" +
                "pair_name="+pairName+
                "&method=limit"+
                "&type="+type+
                "&price="+price+
                "&quantity="+quantity
        );
        String result = request.executeRequest();

        if(result.equals("Error_auth")){
            logger.log("Auth failed");
            return result;
        }else
        if(result.equals("Connection_error")){
            logger.log("Error connecting to the exchange API");
            return result;
        }else{
            logger.log("Limit order with" +
                    " price: "+price+
                    " type: "+type+
                    " quantity: "+quantity
                    +" successfully added | Ticker: "+pairName);
            return "ok";
        }
    }

    public String addMarketOrder(String type, String quantity){
        request.setAuthToken(authToken);
        request.setCommand("order/create?" +
                "pair_name="+pairName+
                "&method=market"+
                "&type="+type+
                "&quantity="+quantity
        );
        String result = request.executeRequest();

        if(result.equals("Error_auth")){
            logger.log("Auth failed");
            return result;
        }else
        if(result.equals("Connection_error")){
            logger.log("Error connecting to the exchange API");
            return result;
        }else{
            logger.log("Market order with" +
                    " type: "+type+
                    " quantity: "+quantity
                    +" successfully added | Ticker: "+pairName);
            return "ok";
        }
    }

    public String getMarketPrice(){

        request.setCommand("ticker/name?pair_name="+pairName);

        String responseJson = request.executeRequest();

        TickerResponseDecoder tickerDecoder = new TickerResponseDecoder(pairName);

        if(responseJson.equals("Connection_error")){


            logger.log("Error get ticker price for pair: " + pairName);

            return "0.0";

        }else{

            tickerDecoder.decode(responseJson);

            logger.log("Tiker: "+pairName+" price = "+tickerDecoder.getTickerPrice());

            return tickerDecoder.getTickerPrice();

        }


    }
    public int getGroupsCount(String type){

        request.setCommand("depth/name?pair_name="+pairName);

        String responseJson = request.executeRequest();




        if(responseJson.equals("Connection_error")){
            logger.log("Warning: No groups on market");
            return 0;
        }else{
            GroupsResponseDecoder grd = new GroupsResponseDecoder();
            grd.decode(responseJson);

            if(type.equals("asks"))
                return grd.getCountAsks();
            if(type.equals("bids"))
                return grd.getCountBids();

        }


        return 0;
    }

}
