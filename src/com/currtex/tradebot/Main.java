package com.currtex.tradebot;

import com.currtex.tradebot.logger.ConsoleLogger;
import com.currtex.tradebot.tradestrategies.BaseTradeStrategy;
import com.currtex.tradebot.tradestrategies.instruments.ITradeStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class Main {

    public static void main(String[] args) {
        // pgrep java

        System.out.println("-------------Version 0.1.0 BETA---------------");
        List<String> pair_names = new ArrayList<>();
        pair_names.add("BTC/USDT");
        pair_names.add("ETH/USDT");
        pair_names.add("XRP/USDT");
        pair_names.add("LTC/USDT");
        pair_names.add("ETC/USDT");
        pair_names.add("DASH/USDT");
        pair_names.add("DOGE/USDT");
        pair_names.add("KMD/USDT");
        pair_names.add("NEO/USDT");
        //pair_names.add("NMC/USDT");
        pair_names.add("XMR/USDT");
        pair_names.add("ZEC/USDT");

        if(!args[0].equals("start")) {
            System.out.print("Enter command to start bot: ");
            Scanner sc = new Scanner(System.in);

            String command = sc.nextLine();




            if (command.equals("start")) {

                pair_names.forEach(pair_name -> botStart(pair_name));
            }

            System.out.println("Bots have stopped working");
        }else{

            pair_names.forEach(pair_name -> botStart(pair_name));


            System.out.println("Bots have stopped working");
        }

    }

    private static void botStart(String pair_name){


        ITradeStrategy tradeStrategy = new BaseTradeStrategy(new ConsoleLogger(), pair_name);
        tradeStrategy.execute();
    }
}
