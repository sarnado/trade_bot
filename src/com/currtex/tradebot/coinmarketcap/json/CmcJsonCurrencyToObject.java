package com.currtex.tradebot.coinmarketcap.json;

import com.currtex.tradebot.coinmarketcap.entity.currencies.CmcCurrency;
import com.google.gson.Gson;

import java.util.List;


public class CmcJsonCurrencyToObject {

    private List<CmcCurrency> currencies;

    public CmcJsonCurrencyToObject(String  jsonCurrencies)
    {

       Gson g = new Gson();

       JsonCurrenciesListingStructure coinMarketCapResponse = g.fromJson(jsonCurrencies, JsonCurrenciesListingStructure.class);

        currencies = coinMarketCapResponse.getData();

    }

    public List<CmcCurrency> getCurrencies() {
        return currencies;
    }
}
