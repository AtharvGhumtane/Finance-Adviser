package com.alexz.cryptonewsAlexz.controller;

import com.alexz.cryptonewsAlexz.dto.NewsDTO;
import com.alexz.cryptonewsAlexz.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NewsController {

    private final NewsService newsService;

    /**
     * Get latest news
     * GET /api/news/latest?limit=20
     */
    @GetMapping("/latest")
    public ResponseEntity<List<NewsDTO>> getLatestNews(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Fetching latest {} news items", limit);
        List<NewsDTO> news = newsService.getLatestNews(limit);
        return ResponseEntity.ok(news);
    }

    /**
     * Get news by cryptocurrency
     * GET /api/news/crypto/BTC?page=0&size=10
     */
    @GetMapping("/crypto/{crypto}")
    public ResponseEntity<Page<NewsDTO>> getNewsByCrypto(
            @PathVariable String crypto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching news for crypto: {}", crypto);
        Page<NewsDTO> news = newsService.getNewsByCrypto(crypto.toUpperCase(), page, size);
        return ResponseEntity.ok(news);
    }

    /**
     * Get recent news (last N hours)
     * GET /api/news/recent?hours=24
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NewsDTO>> getRecentNews(
            @RequestParam(defaultValue = "24") int hours) {
        log.info("Fetching news from last {} hours", hours);
        List<NewsDTO> news = newsService.getRecentNews(hours);
        return ResponseEntity.ok(news);
    }

    /**
     * Manually trigger news fetch (for testing)
     * POST /api/news/fetch
     */
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> triggerNewsFetch() {
        log.info("Manual news fetch triggered");
        try {
            List<NewsDTO> newNews = newsService.fetchAndProcessNews();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "News fetched successfully",
                    "newItemsCount", newNews.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching news", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error fetching news: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check
     * GET /api/news/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "crypto-news-service"
        ));
    }
}
