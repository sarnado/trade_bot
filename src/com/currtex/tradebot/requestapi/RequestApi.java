package com.currtex.tradebot.requestapi;

public interface RequestApi {
    String executeRequest();
    void setCommand(String command);
    void setAuthToken(String token);

}
