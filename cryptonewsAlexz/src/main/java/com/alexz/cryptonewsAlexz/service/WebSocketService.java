package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.dto.NewsDTO;
import com.alexz.cryptonewsAlexz.dto.CryptoPriceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Broadcast price update to all WebSocket subscribers
     */
    public void broadcastPrice(CryptoPriceDTO price) {
        try {
            log.trace("Broadcasting price update via WebSocket: {}", price.getSymbol());
            messagingTemplate.convertAndSend("/topic/crypto-prices", price);
        } catch (Exception e) {
            log.error("Error broadcasting price via WebSocket", e);
        }
    }

    /**
     * Broadcast news to all WebSocket subscribers
     */
    public void broadcastNews(NewsDTO news) {
        try {
            log.debug("Broadcasting news via WebSocket: {}", news.getTitle());
            messagingTemplate.convertAndSend("/topic/crypto-news", news);
        } catch (Exception e) {
            log.error("Error broadcasting news via WebSocket", e);
        }
    }

    /**
     * Broadcast multiple news items
     */
    public void broadcastNewsBatch(List<NewsDTO> newsList) {
        log.info("Broadcasting {} news items via WebSocket", newsList.size());
        newsList.forEach(this::broadcastNews);
    }

    /**
     * Send news to specific cryptocurrency topic
     */
    public void broadcastNewsToCrypto(String crypto, NewsDTO news) {
        try {
            log.debug("Broadcasting {} news via WebSocket", crypto);
            messagingTemplate.convertAndSend("/topic/crypto-news/" + crypto.toUpperCase(), news);
        } catch (Exception e) {
            log.error("Error broadcasting crypto-specific news", e);
        }
    }
}