package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.model.CryptoNews;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAggregatorService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${news.api.cryptocompare.url}")
    private String cryptoCompareUrl;

    /**
     * Fetch news from CryptoCompare API
     */
    public List<CryptoNews> fetchCryptoCompareNews() {
        List<CryptoNews> newsList = new ArrayList<>();

        try {
            log.info("Fetching news from CryptoCompare...");
            String url = cryptoCompareUrl + "?lang=EN";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("Data");

            if (data.isArray()) {
                for (JsonNode newsItem : data) {
                    CryptoNews news = CryptoNews.builder()
                            .newsId("CC_" + newsItem.path("id").asText())
                            .title(newsItem.path("title").asText())
                            .body(newsItem.path("body").asText())
                            .imageUrl(newsItem.path("imageurl").asText())
                            .source(newsItem.path("source").asText("CryptoCompare"))
                            .sourceUrl(newsItem.path("url").asText())
                            .publishedAt(LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(newsItem.path("published_on").asLong()),
                                    ZoneId.systemDefault()))
                            .relatedCryptos(extractCryptos(newsItem.path("tags").asText()))
                            .category("NEWS")
                            .build();

                    newsList.add(news);
                }
            }

            log.info("Fetched {} news items from CryptoCompare", newsList.size());

        } catch (Exception e) {
            log.error("Error fetching CryptoCompare news", e);
        }

        return newsList;
    }

    /**
     * Fetch news from CoinGecko API
     * Note: CoinGecko's free API has limitations. Using alternative approach.
     */
    public List<CryptoNews> fetchCoinGeckoNews() {
        List<CryptoNews> newsList = new ArrayList<>();

        try {
            log.info("Fetching news from CoinGecko...");

            // ✅ FIXED: Removed page parameter that was causing 422 error
            String url = "https://api.coingecko.com/api/v3/news";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // CoinGecko returns data directly in an array
            if (root.isArray()) {
                for (JsonNode newsItem : root) {
                    try {
                        // Parse the news item safely
                        String newsId = newsItem.path("id").asText();
                        String title = newsItem.path("title").asText();
                        String description = newsItem.path("description").asText();

                        // Skip if essential fields are missing
                        if (newsId.isEmpty() || title.isEmpty()) {
                            continue;
                        }

                        // Parse date safely
                        LocalDateTime publishedAt;
                        try {
                            String dateStr = newsItem.path("created_at").asText();
                            if (!dateStr.isEmpty()) {
                                // CoinGecko uses ISO 8601 format
                                publishedAt = LocalDateTime.parse(
                                        dateStr.substring(0, 19),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                );
                            } else {
                                publishedAt = LocalDateTime.now();
                            }
                        } catch (Exception e) {
                            publishedAt = LocalDateTime.now();
                        }

                        CryptoNews news = CryptoNews.builder()
                                .newsId("CG_" + newsId)
                                .title(title)
                                .body(description.isEmpty() ? title : description)
                                .imageUrl(newsItem.path("thumb_2x").asText())
                                .source("CoinGecko")
                                .sourceUrl(newsItem.path("url").asText())
                                .publishedAt(publishedAt)
                                .category("NEWS")
                                .relatedCryptos("GENERAL")
                                .build();

                        newsList.add(news);
                    } catch (Exception e) {
                        log.debug("Skipping problematic news item: {}", e.getMessage());
                    }
                }
            }

            log.info("Fetched {} news items from CoinGecko", newsList.size());

        } catch (Exception e) {
            log.error("Error fetching CoinGecko news: {}", e.getMessage());
            // Don't throw - just return empty list so CryptoCompare news still works
        }

        return newsList;
    }

    /**
     * Extract cryptocurrency symbols from tags
     */
    private String extractCryptos(String tags) {
        if (tags == null || tags.isEmpty()) {
            return "GENERAL";
        }

        // Extract crypto symbols (BTC, ETH, etc.) from tags
        StringBuilder cryptos = new StringBuilder();
        String[] tagArray = tags.split("\\|");

        for (String tag : tagArray) {
            String upper = tag.trim().toUpperCase();
            if (upper.length() >= 3 && upper.length() <= 5) {
                if (cryptos.length() > 0) cryptos.append(",");
                cryptos.append(upper);
            }
        }

        return cryptos.length() > 0 ? cryptos.toString() : "GENERAL";
    }
}