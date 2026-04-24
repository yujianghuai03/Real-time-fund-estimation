package com.yujianghuai.common.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public DirectExchange appExchange() {
        return new DirectExchange(MqConstants.DEFAULT_EXCHANGE, true, false);
    }

    @Bean
    public Queue appQueue() {
        return new Queue(MqConstants.DEFAULT_QUEUE, true);
    }

    @Bean
    public Binding appBinding(Queue appQueue, DirectExchange appExchange) {
        return BindingBuilder.bind(appQueue).to(appExchange).with(MqConstants.DEFAULT_ROUTING_KEY);
    }
}
