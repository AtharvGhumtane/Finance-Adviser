package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.dto.NewsDTO;
import com.alexz.cryptonewsAlexz.model.CryptoNews;
import com.alexz.cryptonewsAlexz.repo.NewsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NewsRepository newsRepository;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to Kafka topic and broadcast to WebSocket clients
     */
    @KafkaListener(
            topics = "${kafka.topic.crypto-news}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeNews(String message) {
        try {
            log.info("📥 Received message from Kafka");

            // Parse the message to NewsDTO
            NewsDTO newsDTO = objectMapper.readValue(message, NewsDTO.class);

            // Check if news already exists in database
            if (newsRepository.existsByNewsId(newsDTO.getNewsId())) {
                log.debug("⚠️ News already exists in database, skipping: {}", newsDTO.getNewsId());
                // Still broadcast to WebSocket for live updates
                webSocketService.broadcastNews(newsDTO);
                log.info("📡 Broadcasted existing news to WebSocket: {}", newsDTO.getTitle());
                return;
            }

            // Save to database (only if new)
            CryptoNews cryptoNews = convertToEntity(newsDTO);
            newsRepository.save(cryptoNews);
            log.info("💾 Saved new news to database: {}", newsDTO.getTitle());

            // Broadcast to WebSocket clients
            webSocketService.broadcastNews(newsDTO);
            log.info("📡 Broadcasted new news to WebSocket: {}", newsDTO.getTitle());

        } catch (Exception e) {
            log.error("❌ Error processing Kafka message: {}", e.getMessage());
            // Don't throw exception to avoid infinite retries
        }
    }

    /**
     * Convert NewsDTO to CryptoNews entity
     */
    private CryptoNews convertToEntity(NewsDTO dto) {
        CryptoNews entity = new CryptoNews();
        entity.setNewsId(dto.getNewsId());
        entity.setTitle(dto.getTitle());
        entity.setBody(dto.getBody());
        entity.setSource(dto.getSource());
        entity.setSourceUrl(dto.getSourceUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setPublishedAt(dto.getPublishedAt());
        entity.setRelatedCryptos(dto.getRelatedCryptos());
        //entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}