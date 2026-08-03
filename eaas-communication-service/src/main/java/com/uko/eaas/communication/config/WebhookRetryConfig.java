package com.uko.eaas.communication.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookRetryConfig {

    public static final String WEBHOOK_EXCHANGE = "webhook.direct";
    public static final String WEBHOOK_DLX = "webhook.dlx";
    public static final String WEBHOOK_QUEUE = "webhook.delivery";
    public static final String WEBHOOK_RETRY_QUEUE = "webhook.retry";
    public static final String WEBHOOK_DLQ = "webhook.dlq";

    // Retry delays in minutes: 1, 5, 30, 120, 1440 (24hrs)
    public static final int[] RETRY_DELAYS_MINUTES = {1, 5, 30, 120, 1440};
    public static final int MAX_RETRIES = 5;

    @Bean
    public DirectExchange webhookExchange() {
        return new DirectExchange(WEBHOOK_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange webhookDlx() {
        return new DirectExchange(WEBHOOK_DLX, true, false);
    }

    @Bean
    public Queue webhookQueue() {
        return QueueBuilder.durable(WEBHOOK_QUEUE)
                .withArgument("x-dead-letter-exchange", WEBHOOK_DLX)
                .withArgument("x-dead-letter-routing-key", WEBHOOK_DLQ)
                .build();
    }

    @Bean
    public Queue webhookRetryQueue() {
        return QueueBuilder.durable(WEBHOOK_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", WEBHOOK_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", WEBHOOK_QUEUE)
                .build();
    }

    @Bean
    public Queue webhookDlq() {
        return QueueBuilder.durable(WEBHOOK_DLQ).build();
    }

    @Bean
    public Binding webhookBinding() {
        return BindingBuilder.bind(webhookQueue())
                .to(webhookExchange())
                .with(WEBHOOK_QUEUE);
    }

    @Bean
    public Binding webhookDlqBinding() {
        return BindingBuilder.bind(webhookDlq())
                .to(webhookDlx())
                .with(WEBHOOK_DLQ);
    }
}
