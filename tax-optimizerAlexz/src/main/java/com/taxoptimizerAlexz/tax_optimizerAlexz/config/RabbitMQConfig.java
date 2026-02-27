package com.taxoptimizerAlexz.tax_optimizerAlexz.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration.
 *
 * Flow:
 *  1. TaxOptimizationService → Producer publishes to [tax.optimizer.exchange]
 *  2. Routing key [tax.recommendation] → routes to [tax.recommendation.queue]
 *  3. TaxRecommendationConsumer picks up message → saves to DB via R2DBC
 *
 * Dead Letter Queue (DLQ) included for failed message handling.
 */
@Configuration
public class RabbitMQConfig {

    // ─── Queue Names ──────────────────────────────────────────────────────────
    @Value("${rabbitmq.queue.tax-recommendation}")
    private String taxRecommendationQueue;

    @Value("${rabbitmq.queue.tax-recommendation-dlq}")
    private String taxRecommendationDLQ;

    // ─── Exchange Names ───────────────────────────────────────────────────────
    @Value("${rabbitmq.exchange.tax-optimizer}")
    private String taxOptimizerExchange;

    @Value("${rabbitmq.exchange.tax-optimizer-dlx}")
    private String taxOptimizerDLX;

    // ─── Routing Keys ─────────────────────────────────────────────────────────
    @Value("${rabbitmq.routing-key.tax-recommendation}")
    private String taxRecommendationRoutingKey;

    @Value("${rabbitmq.routing-key.tax-recommendation-dlq}")
    private String taxRecommendationDLQRoutingKey;

    // ─── Main Queue ───────────────────────────────────────────────────────────

    @Bean
    public Queue taxRecommendationQueue() {
        return QueueBuilder.durable(taxRecommendationQueue)
                .withArgument("x-dead-letter-exchange", taxOptimizerDLX)
                .withArgument("x-dead-letter-routing-key", taxRecommendationDLQRoutingKey)
                .withArgument("x-message-ttl", 86400000) // 24h TTL
                .build();
    }

    // ─── Dead Letter Queue ────────────────────────────────────────────────────

    @Bean
    public Queue taxRecommendationDLQ() {
        return QueueBuilder.durable(taxRecommendationDLQ).build();
    }

    // ─── Exchanges ────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange taxOptimizerExchange() {
        return ExchangeBuilder.topicExchange(taxOptimizerExchange)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange taxOptimizerDLX() {
        return ExchangeBuilder.topicExchange(taxOptimizerDLX)
                .durable(true)
                .build();
    }

    // ─── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding taxRecommendationBinding() {
        return BindingBuilder
                .bind(taxRecommendationQueue())
                .to(taxOptimizerExchange())
                .with(taxRecommendationRoutingKey);
    }

    @Bean
    public Binding taxRecommendationDLQBinding() {
        return BindingBuilder
                .bind(taxRecommendationDLQ())
                .to(taxOptimizerDLX())
                .with(taxRecommendationDLQRoutingKey);
    }

    // ─── Message Converter (JSON) ─────────────────────────────────────────────

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
