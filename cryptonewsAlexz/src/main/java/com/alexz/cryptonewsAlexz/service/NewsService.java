package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.dto.NewsDTO;
import com.alexz.cryptonewsAlexz.model.CryptoNews;
import com.alexz.cryptonewsAlexz.repo.NewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsAggregatorService aggregatorService;
    private final KafkaProducerService kafkaProducerService;
    private final WebSocketService webSocketService;

    @Transactional
    public List<NewsDTO> fetchAndProcessNews() {
        log.info("Starting news fetch cycle...");
        List<CryptoNews> allNews = new ArrayList<>();

        allNews.addAll(aggregatorService.fetchCryptoCompareNews());
        allNews.addAll(aggregatorService.fetchCoinGeckoNews());

        List<CryptoNews> newItems = allNews.stream()
                .filter(news -> !newsRepository.existsByNewsId(news.getNewsId()))
                .collect(Collectors.toList());

        if (newItems.isEmpty()) {
            log.info("No new news items found");
            return List.of();
        }

        List<CryptoNews> savedNews = newsRepository.saveAll(newItems);
        log.info("Saved {} new news items", savedNews.size());

        List<NewsDTO> newsDTOs = savedNews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        kafkaProducerService.publishNewsBatch(newsDTOs);
        webSocketService.broadcastNewsBatch(newsDTOs);

        return newsDTOs;
    }

    public List<NewsDTO> getLatestNews(int limit) {
        return newsRepository.findTop50ByOrderByPublishedAtDesc()
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<NewsDTO> getNewsByCrypto(String crypto, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository
                .findByRelatedCryptosContainingIgnoreCase(crypto, pageable)
                .map(this::convertToDTO);
    }

    public List<NewsDTO> getRecentNews(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return newsRepository.findByPublishedAtAfterOrderByPublishedAtDesc(since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private NewsDTO convertToDTO(CryptoNews news) {
        return NewsDTO.builder()
                .id(news.getId())
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .body(news.getBody())
                .imageUrl(news.getImageUrl())
                .source(news.getSource())
                .sourceUrl(news.getSourceUrl())
                .publishedAt(news.getPublishedAt())
                .relatedCryptos(news.getRelatedCryptos())
                .category(news.getCategory())
                .timeAgo(calculateTimeAgo(news.getPublishedAt()))
                .build();
    }

    private String calculateTimeAgo(LocalDateTime publishedAt) {
        if (publishedAt == null) return "Unknown";

        Duration duration = Duration.between(publishedAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (hours < 24) return hours + " hours ago";
        if (days < 7) return days + " days ago";
        return publishedAt.toString();
    }
}
