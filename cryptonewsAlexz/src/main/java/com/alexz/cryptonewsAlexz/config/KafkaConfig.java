package com.alexz.cryptonewsAlexz.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.crypto-news}")
    private String newsTopic;

    @Value("${kafka.topic.price-updates}")
    private String priceTopic;

    /**
     * Create crypto news topic
     */
    @Bean
    public NewTopic cryptoNewsTopic() {
        return TopicBuilder.name(newsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create price updates topic
     */
    @Bean
    public NewTopic priceUpdatesTopic() {
        return TopicBuilder.name(priceTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
