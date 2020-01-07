package com.currtex.tradebot.coinmarketcap.entity.equivalent;


public class Quote {
    //Добавте сюда название валюты и геттер на нее
    private EquivCurrency USD;
    private EquivCurrency USDT;

    public EquivCurrency getUSDT() {
        return USDT;
    }

    public EquivCurrency getUSD() {
        return USD;
    }

}
