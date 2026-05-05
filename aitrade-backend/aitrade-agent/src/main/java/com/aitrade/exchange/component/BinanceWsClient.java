package com.aitrade.exchange.component;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BinanceWsClient {

    @Autowired
    private OkHttpClient okHttpClient;

    private WebSocket webSocket;

    private static final String URL = "wss://stream.binance.us:9443/ws/btcusdt@kline_1m";

//    @PostConstruct
    public void start() {

        Request request = new Request.Builder()
                .url(URL)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("Binance WebSocket connected");

                // ⚠️ Binance 不需要 send subscribe
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WebSocket error: " + t.getMessage());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("Closed: " + reason);
            }
        });
    }

    private void handleMessage(String message) {
        System.out.println(message);
    }
}
