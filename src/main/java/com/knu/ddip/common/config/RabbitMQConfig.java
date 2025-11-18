package com.knu.ddip.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RabbitMQConfig {

    public static final String UPDATE_USER_COUNT_STATUS_QUEUE = "updateUserCountStatusQueue";
    public static final String UPDATE_USER_COUNT_STATUS_EXCHANGE = "updateUserCountStatusExchange";

    // 유저 수 갱신 큐
    @Bean
    public String dynamicUpdateUserCountStatusQueueName() {
        String randomString = UUID.randomUUID().toString();
        return UPDATE_USER_COUNT_STATUS_QUEUE + " : " + randomString;
    }

    @Bean
    public Queue updateUserCountStatusQueue(@Qualifier("dynamicUpdateUserCountStatusQueueName") String queueName) {
        return new Queue(queueName, false);
    }

    @Bean
    public FanoutExchange updateUserCountStatusExchange() {
        return new FanoutExchange(UPDATE_USER_COUNT_STATUS_EXCHANGE);
    }

    @Bean
    public Binding updateUserCountStatusBinding(
            Queue updateUserCountStatusQueue,
            FanoutExchange updateUserCountStatusExchange
    ) {
        return BindingBuilder.bind(updateUserCountStatusQueue).to(updateUserCountStatusExchange);
    }

    // 직렬화, 역직렬화 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        return factory;
    }
}
