package com.aitrade.exchange.handler;

public interface RecoveryHandler {
    void onComplete(String symbol);
    void onError(String symbol, Throwable t);

    void onProgress(String symbol, int progress);
}
