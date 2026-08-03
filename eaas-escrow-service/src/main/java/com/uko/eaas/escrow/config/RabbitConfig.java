package com.uko.eaas.escrow.config;

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
    public static final String ESCROW_USER_QUEUE = "escrow.user.events";
    public static final String ESCROW_USER_DLQ = "escrow.user.events.dlq";
    public static final String ESCROW_PAYMENT_QUEUE = "escrow.payment.events";
    public static final String ESCROW_PAYMENT_DLQ = "escrow.payment.events.dlq";

    @Bean
    public TopicExchange eaasExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange eaasDlx() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue escrowUserQueue() {
        return QueueBuilder.durable(ESCROW_USER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", ESCROW_USER_DLQ)
                .build();
    }

    @Bean
    public Queue escrowUserDlq() {
        return QueueBuilder.durable(ESCROW_USER_DLQ).build();
    }

    @Bean
    public Binding escrowUserBinding() {
        return BindingBuilder.bind(escrowUserQueue())
                .to(eaasExchange())
                .with("user.*");
    }

    @Bean
    public Binding escrowUserDlqBinding() {
        return BindingBuilder.bind(escrowUserDlq())
                .to(eaasDlx())
                .with(ESCROW_USER_DLQ);
    }

    @Bean
    public Queue escrowPaymentQueue() {
        return QueueBuilder.durable(ESCROW_PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", ESCROW_PAYMENT_DLQ)
                .build();
    }

    @Bean
    public Queue escrowPaymentDlq() {
        return QueueBuilder.durable(ESCROW_PAYMENT_DLQ).build();
    }

    @Bean
    public Binding escrowPaymentBinding() {
        return BindingBuilder.bind(escrowPaymentQueue())
                .to(eaasExchange())
                .with("payment.*");
    }

    @Bean
    public Binding escrowPaymentDlqBinding() {
        return BindingBuilder.bind(escrowPaymentDlq())
                .to(eaasDlx())
                .with(ESCROW_PAYMENT_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper() {
            @Override
            public void fromJavaType(JavaType javaType, MessageProperties properties) {
                // Don't write __TypeId__ to avoid cross-service classpath issues.
                // Each service has its own copy of event classes in a different package.
                // Consumers rely on the @RabbitListener method parameter type or parse
                // the body as raw JSON via the eventType field.
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