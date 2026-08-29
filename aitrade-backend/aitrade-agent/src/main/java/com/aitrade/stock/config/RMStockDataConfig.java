package com.aitrade.stock.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RMStockDataConfig {
    public static final String EXCHANGE_NAME = "stock.data.exchange";
    public static final String QUEUE_NAME = "stock.data.queue";
    public static final String ROUTING_KEY = "stock.data.routingKey";

    // 声明持久化队列
    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    // 声明直连交换机
    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // 将队列与交换机通过路由键绑定
    @Bean
    public Binding bindingStock(Queue stockQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockQueue).to(stockExchange).with(ROUTING_KEY);
    }
}
