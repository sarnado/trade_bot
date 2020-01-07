package com.currtex.tradebot.logger;

public class ConsoleLogger implements ILogger {

    @Override
    public void log(String loggerData) {
        System.out.println(loggerData);
    }
}
