package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.dto.NewsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.crypto-news}")
    private String newsTopic;

    /**
     * Publish news to Kafka topic
     */
    public void publishNews(NewsDTO news) {
        try {
            log.debug("Publishing news to Kafka: {}", news.getTitle());
            kafkaTemplate.send(newsTopic, news.getNewsId(), news);
            log.debug("News published successfully");
        } catch (Exception e) {
            log.error("Error publishing news to Kafka", e);
        }
    }

    /**
     * Publish multiple news items
     */
    public void publishNewsBatch(List<NewsDTO> newsList) {
        log.info("Publishing {} news items to Kafka", newsList.size());
        newsList.forEach(this::publishNews);
    }
}
