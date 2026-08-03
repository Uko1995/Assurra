package com.uko.eaas.payment.config;

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
    public static final String PAYMENT_ESCROW_QUEUE = "payment.escrow.triggers";
    public static final String PAYMENT_ESCROW_DLQ = "payment.escrow.triggers.dlq";
    public static final String PAYMENT_USER_QUEUE = "payment.user.events";
    public static final String PAYMENT_USER_DLQ = "payment.user.events.dlq";

    @Bean
    public TopicExchange eaasExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange eaasDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue paymentEscrowQueue() {
        return QueueBuilder.durable(PAYMENT_ESCROW_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", PAYMENT_ESCROW_DLQ)
                .build();
    }

    @Bean
    public Queue paymentEscrowDlq() {
        return QueueBuilder.durable(PAYMENT_ESCROW_DLQ).build();
    }

    @Bean
    public Binding paymentEscrowBinding() {
        return BindingBuilder.bind(paymentEscrowQueue())
                .to(eaasExchange())
                .with("escrow.confirmed");
    }

    @Bean
    public Binding paymentEscrowAutoReleasedBinding() {
        return BindingBuilder.bind(paymentEscrowQueue())
                .to(eaasExchange())
                .with("escrow.auto-released");
    }

    @Bean
    public Binding paymentEscrowResolvedMerchantBinding() {
        return BindingBuilder.bind(paymentEscrowQueue())
                .to(eaasExchange())
                .with("escrow.resolved-merchant");
    }

    @Bean
    public Binding paymentEscrowResolvedCustomerBinding() {
        return BindingBuilder.bind(paymentEscrowQueue())
                .to(eaasExchange())
                .with("escrow.resolved-customer");
    }

    @Bean
    public Binding paymentEscrowDlqBinding() {
        return BindingBuilder.bind(paymentEscrowDlq())
                .to(eaasDlx())
                .with(PAYMENT_ESCROW_DLQ);
    }

    @Bean
    public Queue paymentUserQueue() {
        return QueueBuilder.durable(PAYMENT_USER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", PAYMENT_USER_DLQ)
                .build();
    }

    @Bean
    public Queue paymentUserDlq() {
        return QueueBuilder.durable(PAYMENT_USER_DLQ).build();
    }

    @Bean
    public Binding paymentUserBinding() {
        return BindingBuilder.bind(paymentUserQueue())
                .to(eaasExchange())
                .with("user.*");
    }

    @Bean
    public Binding paymentUserDlqBinding() {
        return BindingBuilder.bind(paymentUserDlq())
                .to(eaasDlx())
                .with(PAYMENT_USER_DLQ);
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
