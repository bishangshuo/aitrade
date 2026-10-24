package com.aitrade.stock.handler;

public interface ActionMessageListener {
    void onComplete();
    void onFail(String message);
    void onBuy();
    void onSell();
}
