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

public class ShitStrategy implements ITradeStrategy {

    private ILogger logger;
    private volatile Double cmcTickerPrice = 0.0;
    private volatile Double exchangeTickerPrice = 0.0;
    private final int ORDER_COUNT = 10;
    private final double AMPLIFIER = 0.8; //Усилитель ордера по направлению
    private volatile boolean isRise = true;
    private final double MAX_PERCENTAGE = 0.003;
    private ExchangeApi eApi;
    private Double minQuantity = 0.09;
    private Double maxQuantity = 0.3;
    private Double minMarketQuantity = 0.009;
    private Double maxMarketQuantity = 0.03;
    private final int POWERFUL_MARKET = 20;
    private String pair_name;

    public ShitStrategy(ILogger logger, String pair_name) {
        this.logger = logger;
        this.pair_name = pair_name;
    }

    @Override
    public void execute() {

        eApi = new ExchangeApi(new RequestExchange(), logger);

        // Получаем стаканы
        // Если пустые выставляем ордера на покупку и на продажу
        // Торгуем минутку. Выставляем лимитный ордер близкий к рынку и в направлении его выкупаем.

        cmcTickerPrice = 7500.0;//getCmcPrice();
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
                    int time = 1000*60*getRandomBeetwen(1, 15);
                    try{
                        int rand = getRandomBeetwen(1, 2);
                        if(rand > 1) {
                            cmcTickerPrice += cmcTickerPrice * (getRandomBeetwen(1, 10)) / 100;
                        }else{
                            cmcTickerPrice -= cmcTickerPrice * (getRandomBeetwen(1, 10)) / 100;
                        }
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
                    int time = 1000*60*4;
                    final double minProbability = 0.6;
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


                        int time = getRandomBeetwen(3,10)*1000;
                        int marketOrderCount = getRandomBeetwen(3, 7);


                        boolean quantityUpdated = false;

                        while(!quantityUpdated){
                            quantityUpdated = calculateMinMaxQuantity();
                        }
                        fillLimitOrders(1);

                        double prevPrice = exchangeTickerPrice;
                        for (int i = 1; i < marketOrderCount; i++) {

                            String typeLastMarketOrder = executeMarketOrder(pair_name);
                            exchangeTickerPrice = getExchangeTickerPrice();
                            fillLimitsByCandle(typeLastMarketOrder);
                            Thread.sleep(1000);
                        }
                        if(exchangeTickerPrice == prevPrice){
                            isRise = !isRise;// Меняем направление
                        }
                        //fillLimitsByCandle(typeLastMarketOrder);
                        logger.log("\u001B[32m Direction is rise: "+isRise+"\u001B[0m");










                        Thread.sleep(time);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread limitOrdersSetter = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        fillLimitOrders(1.3);
                        Thread.sleep(10000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


            }

        });

        directionDecision.start();
        baseTradeThread.start();
        cmcListener.start();
        limitOrdersSetter.start();


    }


    private void fillLimitsByCandle(String type) throws InterruptedException {
        double scale;
        for (int i = 0; i < 2; i++) {

            scale = (double) getRandomBeetwen(i+1, 10)/100;
            if(type.equals("buy")) {

                executeLimitOrder(pair_name, "buy", scale);
            }else{
                executeLimitOrder(pair_name, "sell", scale);
            }
            int sleepTime = getRandomBeetwen(1, 2)*1000;
            Thread.sleep(sleepTime);
        }
        logger.log("\u001B[31m Candle fixed \u001B[0m");
    }
    private void fillLimitOrders(double priceScale) throws InterruptedException{
        eApi.setPairName(pair_name);
        int asksCount = eApi.getGroupsCount("asks");
        int bidsCount = eApi.getGroupsCount("bids");



        if(asksCount < ORDER_COUNT){
            for (int i = 0; i < ORDER_COUNT; i++){
                //Покупка
                executeLimitOrder(pair_name,"sell", priceScale);
                int sleepTime = getRandomBeetwen(1, 2)*1000;
                Thread.sleep(sleepTime);
            }
            logger.log("Tiker: "+pair_name+" orders \u001B[31m asks FILLED \u001B[0m");
        }
        if(bidsCount < ORDER_COUNT){
            for (int i = 0; i < ORDER_COUNT; i++){
                //Продажа
                executeLimitOrder(pair_name,"buy", priceScale);
                int sleepTime = getRandomBeetwen(1, 2)*1000;
                Thread.sleep(sleepTime);
            }
            logger.log("Tiker: "+pair_name+" orders \u001B[31m bids FILLED \u001B[0m");
        }

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


    private void executeLimitOrder(String pairName, String type , double priceScale){

        Double orderPrice;


        double percentage = (Math.random() * MAX_PERCENTAGE)*priceScale;

        if(type.equals("sell")){
            orderPrice = exchangeTickerPrice + (exchangeTickerPrice * percentage);

        }else{
            orderPrice = exchangeTickerPrice - (exchangeTickerPrice * percentage);

        }


        Double quantity = minQuantity + Math.random()*maxQuantity;

        String strQuantity = String.format("%.4f", quantity);
        strQuantity = strQuantity.replace(',','.');

        String strPrice = String.format("%.4f", orderPrice);
        strPrice = strPrice.replace(',','.');

        eApi.setPairName(pairName);
        eApi.addLimitOrder(type, strPrice, strQuantity);

    }

    private String executeMarketOrder(String pairName){


        String type;

        double chance = 0.75;
        double random = Math.random();


        Double quantity = minMarketQuantity + Math.random()*maxMarketQuantity;

        if(isRise){
            if(chance >= random) {
                type = "buy";
            }else{
                type = "sell";

            }

            if (type.equals("sell")){
                quantity -= (quantity * AMPLIFIER);
            }else{
                quantity += (quantity * AMPLIFIER);
            }
        }else{
            if(chance < random) {
                type = "buy";
            }else{
                type = "sell";

            }

            if (type.equals("buy")){
                quantity -= (quantity * AMPLIFIER);
            }else{quantity += (quantity * AMPLIFIER);}
        }


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
