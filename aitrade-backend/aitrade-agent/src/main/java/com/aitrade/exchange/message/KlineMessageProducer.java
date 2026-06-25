package com.aitrade.exchange.message;

import com.aitrade.exchange.config.RabbitMQConfig;
import com.aitrade.exchange.domain.Kline;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KlineMessageProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendKlineMessage(Kline kline) {
        // 参数分别为：交换机名称、路由键、消息内容
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                kline
        );
        System.out.println("kline消息发送成功: " + kline);
    }
}
