package com.yujianghuai.common.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MqProducer {

    private static final Logger log = LoggerFactory.getLogger(MqProducer.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    public MqProducer(ObjectProvider<RabbitTemplate> rabbitTemplateProvider) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
    }

    public void send(Object payload) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate unavailable, skip sending message: {}", payload);
            return;
        }

        rabbitTemplate.convertAndSend(
                MqConstants.DEFAULT_EXCHANGE,
                MqConstants.DEFAULT_ROUTING_KEY,
                payload
        );
    }
}
