package com.currtex.tradebot.coinmarketcap.json;


import com.currtex.tradebot.coinmarketcap.entity.currencies.CmcCurrency;


import java.util.List;


public class JsonCurrenciesListingStructure {

    private List<CmcCurrency> data;

    public List<CmcCurrency> getData() {
        return data;
    }

}
