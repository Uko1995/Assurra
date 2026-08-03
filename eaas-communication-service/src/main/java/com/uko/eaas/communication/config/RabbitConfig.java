package com.uko.eaas.communication.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "eaas.exchange";
    public static final String DLX_NAME = "eaas.dlx";
    public static final String COMM_NOTIFICATIONS_QUEUE = "comm.notifications";
    public static final String COMM_NOTIFICATIONS_DLQ = "comm.notifications.dlq";

    @Bean
    public TopicExchange eaasExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange eaasDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue commNotificationsQueue() {
        return QueueBuilder.durable(COMM_NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", COMM_NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue commNotificationsDlq() {
        return QueueBuilder.durable(COMM_NOTIFICATIONS_DLQ).build();
    }

    @Bean
    public Binding commEscrowBinding() {
        return BindingBuilder.bind(commNotificationsQueue())
                .to(eaasExchange())
                .with("escrow.*");
    }

    @Bean
    public Binding commPaymentBinding() {
        return BindingBuilder.bind(commNotificationsQueue())
                .to(eaasExchange())
                .with("payment.*");
    }

    @Bean
    public Binding commPayoutBinding() {
        return BindingBuilder.bind(commNotificationsQueue())
                .to(eaasExchange())
                .with("payout.*");
    }

    @Bean
    public Binding commUserBinding() {
        return BindingBuilder.bind(commNotificationsQueue())
                .to(eaasExchange())
                .with("user.*");
    }

    @Bean
    public Binding commNotificationsDlqBinding() {
        return BindingBuilder.bind(commNotificationsDlq())
                .to(eaasDlx())
                .with(COMM_NOTIFICATIONS_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper() {
            @Override
            public void fromJavaType(JavaType javaType, MessageProperties properties) {
            }
        };
        converter.setJavaTypeMapper(typeMapper);
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
