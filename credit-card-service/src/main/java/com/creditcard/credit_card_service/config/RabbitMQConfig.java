package com.creditcard.credit_card_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for credit card analysis async flow.
 *
 *  HTTP Response → Producer → [credit.card.exchange]
 *    → [credit.analysis.queue]
 *    → Consumer → R2DBC save
 *
 *  Failed messages → Dead Letter Exchange → [credit.analysis.dlq]
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.credit-analysis}")
    private String analysisQueue;

    @Value("${rabbitmq.queue.credit-analysis-dlq}")
    private String analysisDLQ;

    @Value("${rabbitmq.exchange.credit-card}")
    private String creditCardExchange;

    @Value("${rabbitmq.exchange.credit-card-dlx}")
    private String creditCardDLX;

    @Value("${rabbitmq.routing-key.credit-analysis}")
    private String analysisRoutingKey;

    @Value("${rabbitmq.routing-key.credit-analysis-dlq}")
    private String analysisDLQRoutingKey;

    // ─── Queues ───────────────────────────────────────────────────────────────

    @Bean
    public Queue creditAnalysisQueue() {
        return QueueBuilder.durable(analysisQueue)
                .withArgument("x-dead-letter-exchange", creditCardDLX)
                .withArgument("x-dead-letter-routing-key", analysisDLQRoutingKey)
                .withArgument("x-message-ttl", 86400000) // 24h TTL
                .build();
    }

    @Bean
    public Queue creditAnalysisDLQ() {
        return QueueBuilder.durable(analysisDLQ).build();
    }

    // ─── Exchanges ────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange creditCardExchange() {
        return ExchangeBuilder.topicExchange(creditCardExchange).durable(true).build();
    }

    @Bean
    public TopicExchange creditCardDLX() {
        return ExchangeBuilder.topicExchange(creditCardDLX).durable(true).build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding creditAnalysisBinding() {
        return BindingBuilder.bind(creditAnalysisQueue())
                .to(creditCardExchange()).with(analysisRoutingKey);
    }

    @Bean
    public Binding creditAnalysisDLQBinding() {
        return BindingBuilder.bind(creditAnalysisDLQ())
                .to(creditCardDLX()).with(analysisDLQRoutingKey);
    }

    // ─── Converter & Template ─────────────────────────────────────────────────

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
