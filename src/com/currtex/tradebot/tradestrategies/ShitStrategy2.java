package com.currtex.tradebot.tradestrategies;

import com.currtex.tradebot.coinmarketcap.entity.currencies.CmcCurrency;
import com.currtex.tradebot.coinmarketcap.json.CmcJsonCurrencyToObject;
import com.currtex.tradebot.exchangeapi.ExchangeApi;
import com.currtex.tradebot.logger.ILogger;
import com.currtex.tradebot.requestapi.CmcRequestCreator;
import com.currtex.tradebot.requestapi.RequestApi;
import com.currtex.tradebot.requestapi.RequestExchange;
import com.currtex.tradebot.tradestrategies.instruments.ITradeStrategy;

import java.util.List;

public class ShitStrategy2 implements ITradeStrategy {

    private ILogger logger;
    private volatile Double cmcTickerPrice = 0.0;
    private volatile Double exchangeTickerPrice = 0.0;
    private final int ORDER_COUNT = 10;
    private final double AMPLIFIER = 0.4; //Усилитель ордера по направлению
    private volatile boolean isRise = true;
    private final double MAX_PERCENTAGE = 0.0002;
    private final double SCALE = 1.00;
    private volatile boolean isMarketReady = false;
    private ExchangeApi eApi;
    private Double minQuantity = 0.009;
    private Double maxQuantity = 0.03;
    private Double minMarketQuantity = 0.01;
    private Double maxMarketQuantity = 0.05;
    private String pair_name;

    public ShitStrategy2(ILogger logger, String pair_name) {
        this.logger = logger;
        this.pair_name = pair_name;
    }

    @Override
    public void execute() {

        eApi = new ExchangeApi(new RequestExchange(), logger);

        //TODO: если ордер в течении

        cmcTickerPrice = getCmcPrice();//getCmcPrice();
        exchangeTickerPrice = getExchangeTickerPrice();


        if(exchangeTickerPrice == 0){
            exchangeTickerPrice = cmcTickerPrice;
        }

        if (cmcTickerPrice > exchangeTickerPrice) {
            isRise = true;
        } else {
            isRise = false;
        }
        Thread cmcListener = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    int time = 1000*60*60;
                    try{
                        cmcTickerPrice = getCmcPrice();//getCmcPrice();
                        logger.log("\u001B[32mTarget price received pirce: "+cmcTickerPrice+"\u001B[0m | Tcker: \u001B[33m"+pair_name+"\u001B[0m");
                        Thread.sleep(time);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
        Thread directionDecision = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int time = 1000*60*1;
                    final double minProbability = 0.85;
                    double currentProbabbity = Math.random();

                    try {
                        if (cmcTickerPrice > exchangeTickerPrice) {
                            if(minProbability >= currentProbabbity) {
                                isRise = true;
                            }else{
                                isRise = false;
                            }
                        } else {
                            if(minProbability >= currentProbabbity) {

                                isRise = false;
                            }else{
                                isRise = true;
                            }

                        }

                        Thread.sleep(time);
                    }catch (Exception e){ e.printStackTrace();}

                }
            }
        });
        Thread baseTradeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {

                        double chance = 0.85;
                        double rand = Math.random();
                    int time = getRandomBeetwen(10,20)*1000;

                    String typeS;
                    Double targetPrice;

                    if(isRise){

                        if(chance >= rand) {

                            typeS = "buy";
                            targetPrice = executeLimitOrder(pair_name, "sell", SCALE, AMPLIFIER*1.5);

                        }else{
                            typeS = "sell";
                            targetPrice = executeLimitOrder(pair_name, "buy", SCALE, AMPLIFIER*1.5);

                        }
                    }else{
                        if(chance <= rand) {

                            typeS = "buy";
                            targetPrice = executeLimitOrder(pair_name, "sell", SCALE, AMPLIFIER*1.5);


                        }else{

                            typeS = "sell";
                            targetPrice = executeLimitOrder(pair_name, "buy", SCALE, AMPLIFIER*1.5);

                        }
                    }

                    executeMarketOrder(pair_name, typeS);

                    /*Double marketPrice = getExchangeTickerPrice();
                    logger.log("--"+typeS);
                    if(typeS.equals("sell")) {

                        while (marketPrice >= targetPrice) {
                            marketPrice = getExchangeTickerPrice();
                            int timeMarket = getRandomBeetwen(3,10)*100;
                            executeMarketOrder(pair_name, typeS);

                            Thread.sleep(timeMarket);
                        }
                    }
                    if(typeS.equals("buy")) {
                        while (marketPrice <= targetPrice) {
                            marketPrice = getExchangeTickerPrice();
                            int timeMarket = getRandomBeetwen(3,10)*100;

                            Thread.sleep(timeMarket);
                        }
                    }*/



                    Thread.sleep(time);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread limitOrderFiller = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (true){

                            fillLimitsWithStep(SCALE*1.0);

                        Thread.sleep(7000);
                    }
                }catch (Exception e){ e.printStackTrace();}
            }
        });
        directionDecision.start();
        baseTradeThread.start();
        cmcListener.start();
        //limitOrderFiller.start();


    }

    private void fillLimitsWithStep(Double priceScale){
        eApi.setPairName(pair_name);




            for (int i = 0; i < ORDER_COUNT; i++){
                //Покупка
                executeLimitOrder(pair_name,"sell", priceScale, 0.0);
                executeLimitOrder(pair_name,"buy", priceScale, 0.0);



            }
            logger.log("Tiker: \u001B[31m"+pair_name+"\u001B[0m orders asks FILLED ");
            logger.log("Tiker: \u001B[31m"+pair_name+"\u001B[0m orders bids FILLED ");




    }



    private int getRandomBeetwen(int from, int to){
        return (from+(int) (Math.random() * to));
    }


    private Double getCmcPrice()
    {
        RequestApi rc = new CmcRequestCreator(logger);

        rc.setCommand("cryptocurrency/listings/latest?convert=USDT");

        CmcJsonCurrencyToObject creator = new CmcJsonCurrencyToObject(rc.executeRequest());

        String cmcTickerPrice = "0";
        String[] symbols = pair_name.split("/");

        String currency = symbols[0];
        List<CmcCurrency> currencies = creator.getCurrencies();

        for (CmcCurrency cmcCurrency : currencies) {
            if(cmcCurrency.getSymbol().equals(currency)){
                cmcTickerPrice = cmcCurrency.getQuote().getUSDT().getPrice();
            }
        }




        return Double.parseDouble(cmcTickerPrice);
    }
    private Double getExchangeTickerPrice(){

        eApi.setPairName(pair_name);
        String tickerPrice = eApi.getMarketPrice();

        return Double.parseDouble(tickerPrice);

    }




    private Double executeLimitOrder(String pairName, String type , double priceScale, double amplifier){

        Double orderPrice;

        exchangeTickerPrice = getExchangeTickerPrice();

        double percentage = (Math.random() * MAX_PERCENTAGE)*priceScale;

        if(type.equals("sell")){
            orderPrice = exchangeTickerPrice + (exchangeTickerPrice * percentage);

        }else{
            orderPrice = exchangeTickerPrice - (exchangeTickerPrice * percentage);

        }


        Double quantity = (minQuantity + Math.random()*maxQuantity);
        quantity += quantity*amplifier;

        String strQuantity = String.format("%.4f", quantity);
        strQuantity = strQuantity.replace(',','.');

        String strPrice = String.format("%.4f", orderPrice);
        strPrice = strPrice.replace(',','.');

        eApi.setPairName(pairName);
        eApi.addLimitOrder(type, strPrice, strQuantity);
        return orderPrice;
    }

    private String executeMarketOrder(String pairName, String type){






        Double quantity = minMarketQuantity + Math.random()*maxMarketQuantity;


        quantity += (quantity * AMPLIFIER);

        String strQuantity = String.format("%.4f", quantity);

        strQuantity = strQuantity.replace(',','.');


        eApi.setPairName(pairName);
        eApi.addMarketOrder(type, strQuantity);
        return type;
    }
    private boolean calculateMinMaxQuantity(){
        Double minPrice = 15.0;

        if(exchangeTickerPrice * minQuantity <= minPrice){
            minQuantity *= 10;
            maxQuantity *= 11;
            minMarketQuantity *= 10;
            maxMarketQuantity *= 11;

            return false;
        }
        return true;
    }

}
