package com.currtex.tradebot.coinmarketcap.entity.currencies;


import com.currtex.tradebot.coinmarketcap.entity.equivalent.Quote;

public class CmcCurrency {

    private Integer id;
    private Quote quote;
    private String symbol;
    private String name;

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public Quote getQuote() {
        return quote;
    }

    public Integer getId() {
        return id;
    }

}
