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

public class BaseTradeStrategy implements ITradeStrategy {

    private ILogger logger;
    private volatile Double cmcTickerPrice = 0.0;
    private volatile Double exchangeTickerPrice = 0.0;
    private final int ORDER_COUNT = 20;


    private double amplifier = 2.0; //Усилитель ордера по направлению

    private final double SUPER_AMPLIFER_CHANCE = 0.1;
    private final double SIMPLE_AMPLIFIER_VALUE = 2.0;
    private final double SUPER_AMPLIFIER_VALUE = 8.0;
    private final int CMC_CHECK_PERIOD = 50; // In minutes
    private volatile boolean isRiseMainTrend = true;
    private volatile boolean isRiseFiveMinuteTrend = true;
    private volatile boolean isRise = true;
    private final double MAX_PERCENTAGE = 0.0002;
    private final double RISE_CHANCE = 0.75;
    private double scale = 1.00;
    private ExchangeApi eApi;
    private Double minQuantity = 0.009;
    private Double maxQuantity = 0.03;
    private Double minMarketQuantity = 0.01;
    private Double maxMarketQuantity = 0.05;
    private String pair_name;

    // TODO: Если график не может пробится делать ему амплифай несколько раз подрят
    public BaseTradeStrategy(ILogger logger, String pair_name) {
        this.logger = logger;
        this.pair_name = pair_name;
    }

    @Override
    public void execute() {

        eApi = new ExchangeApi(new RequestExchange(), logger);



        cmcTickerPrice = getCmcPrice();
        exchangeTickerPrice = getExchangeTickerPrice();


        if(exchangeTickerPrice == 0){
            exchangeTickerPrice = cmcTickerPrice;
        }

        if (cmcTickerPrice > exchangeTickerPrice) {
            isRiseMainTrend = true;
        } else {
            isRiseMainTrend = false;
        }

        for (int i = 0; i < ORDER_COUNT; i++) {

            scale = 1.0+Math.random()*2.3;
            executeLimitOrder(pair_name, "buy", scale, 0.0);
            executeLimitOrder(pair_name, "sell", scale, 0.0);

        }
        boolean isQuantityCalculated = false;

        while (!isQuantityCalculated){
            isQuantityCalculated = calculatedMinMaxQuantity();
        }
        Thread fiveMinDecisions = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true){
                        int minitTimeRand = getRandomBeetwen(3, 7);
                        double rand = Math.random();
                        if((isRiseMainTrend && (RISE_CHANCE >= rand)) || (!isRiseMainTrend && (RISE_CHANCE < rand)))
                        {
                            isRiseFiveMinuteTrend = true;
                        }
                        if((isRiseMainTrend && (RISE_CHANCE < rand)) || (!isRiseMainTrend && (RISE_CHANCE >= rand)))
                        {
                            isRiseFiveMinuteTrend = false;
                        }

                        Thread.sleep(1000*60*minitTimeRand);
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        });
        Thread cmcListener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        cmcTickerPrice = getCmcPrice();

                        Thread.sleep(1000*60*CMC_CHECK_PERIOD);
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        });
        Thread marketDecisions = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int time = 1000*10;
                    while(true){
                        double rand = Math.random();


                        if((isRiseFiveMinuteTrend && (RISE_CHANCE >= rand)) || (!isRiseFiveMinuteTrend && (RISE_CHANCE < rand)))
                        {
                            isRise = true;
                        }
                        if((isRiseFiveMinuteTrend && (RISE_CHANCE < rand)) || (!isRiseFiveMinuteTrend && (RISE_CHANCE >= rand)))
                        {
                            isRise = false;
                        }


                        Thread.sleep(time);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Thread tr = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    int time = getRandomBeetwen(7,12); // seconds
                    try {


                        String type;


                       if(isRise){
                           type = "buy";
                       }else{
                           type = "sell";
                       }

                        double rand = Math.random();
                        if(SUPER_AMPLIFER_CHANCE >= rand){
                            amplifier = SUPER_AMPLIFIER_VALUE;
                        }else{
                            amplifier = SIMPLE_AMPLIFIER_VALUE;
                        }




                        executeMarketOrder(pair_name, type);

                        Thread.sleep(1000*time);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        Thread limit = new Thread(new Runnable() {
            @Override
            public void run() {
                try {


                    while (true){
                        for (int i = 0; i < ORDER_COUNT; i++) {


                            scale = 0.9 + Math.random() * 2.3;
                            executeLimitOrder(pair_name, "buy", scale, 0.0);
                            executeLimitOrder(pair_name, "sell", scale, 0.0);



                        }
                        Thread.sleep(1000*15);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        marketDecisions.start();
        cmcListener.start();
        limit.start();
        fiveMinDecisions.start();
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

        double rand = Math.random();
        double chance = 0.5;

        if(isRiseFiveMinuteTrend && type.equals("buy")) {

            if(chance >= rand) {
                quantity += (quantity * amplifier);
            }
        }

        if(!isRiseFiveMinuteTrend && type.equals("sell")) {
            if(chance >= rand) {
                quantity += (quantity * amplifier);
            }
        }


        String strQuantity = String.format("%.4f", quantity);

        strQuantity = strQuantity.replace(',','.');


        eApi.setPairName(pairName);
        eApi.addMarketOrder(type, strQuantity);
        return type;
    }
    private boolean calculatedMinMaxQuantity(){
        Double minPrice = 15.0;

        if(exchangeTickerPrice * minQuantity <= minPrice){
            minQuantity *= 2;
            maxQuantity *= 2;
            minMarketQuantity *= 2;
            maxMarketQuantity *= 2;

            return false;
        }
        return true;
    }

}
