package com.uko.eaas.identity.config;

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
    public static final String IDENTITY_AUDIT_QUEUE = "identity.audit.events";
    public static final String IDENTITY_AUDIT_DLQ = "identity.audit.events.dlq";

    @Bean
    public TopicExchange eaasExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange eaasDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue identityAuditQueue() {
        return QueueBuilder.durable(IDENTITY_AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", IDENTITY_AUDIT_DLQ)
                .build();
    }

    @Bean
    public Queue identityAuditDlq() {
        return QueueBuilder.durable(IDENTITY_AUDIT_DLQ).build();
    }

    @Bean
    public Binding identityEscrowBinding() {
        return BindingBuilder.bind(identityAuditQueue())
                .to(eaasExchange())
                .with("escrow.*");
    }

    @Bean
    public Binding identityPaymentBinding() {
        return BindingBuilder.bind(identityAuditQueue())
                .to(eaasExchange())
                .with("payment.*");
    }

    @Bean
    public Binding identityPayoutBinding() {
        return BindingBuilder.bind(identityAuditQueue())
                .to(eaasExchange())
                .with("payout.*");
    }

    @Bean
    public Binding identityAuditEventBinding() {
        return BindingBuilder.bind(identityAuditQueue())
                .to(eaasExchange())
                .with("audit.event");
    }

    @Bean
    public Binding identityUserBinding() {
        return BindingBuilder.bind(identityAuditQueue())
                .to(eaasExchange())
                .with("user.*");
    }

    @Bean
    public Binding identityAuditDlqBinding() {
        return BindingBuilder.bind(identityAuditDlq())
                .to(eaasDlx())
                .with(IDENTITY_AUDIT_DLQ);
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
