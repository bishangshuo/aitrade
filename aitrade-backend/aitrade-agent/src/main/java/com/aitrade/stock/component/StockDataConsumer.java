package com.aitrade.stock.component;

import com.aitrade.stock.config.RMStockDataConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockDataConsumer {


    @RabbitListener(
            queues = RMStockDataConfig.QUEUE_NAME
    )
    public void onStockDataMessage(
            org.springframework.amqp.core.Message message
    ) {

        String body =
                new String(
                        message.getBody(),
                        java.nio.charset.StandardCharsets.UTF_8
                );

        log.info("raw message={}", body);
    }
}
