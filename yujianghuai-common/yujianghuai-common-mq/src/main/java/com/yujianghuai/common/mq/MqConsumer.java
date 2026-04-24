package com.yujianghuai.common.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MqConsumer {

    private static final Logger log = LoggerFactory.getLogger(MqConsumer.class);

    @RabbitListener(queues = MqConstants.DEFAULT_QUEUE)
    public void consume(String message) {
        log.info("Receive mq message: {}", message);
    }
}
