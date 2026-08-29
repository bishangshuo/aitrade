package com.aitrade.exchange.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RMCurrencyKlineConfig {
    public static final String EXCHANGE_NAME = "kline.message.exchange";
    public static final String QUEUE_NAME = "kline.message.queue";
    public static final String ROUTING_KEY = "kline.message.routingKey";

    // 声明持久化队列
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    // 声明直连交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // 将队列与交换机通过路由键绑定
    @Bean
    public Binding bindingOrder(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        // 💡 替換為新類別
        return new JacksonJsonMessageConverter();
    }
}
