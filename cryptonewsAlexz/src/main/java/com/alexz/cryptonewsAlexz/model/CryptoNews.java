package com.alexz.cryptonewsAlexz.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_news")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String newsId; // External news ID to prevent duplicates

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT") // ✅ CHANGED: No length limit
    private String body;

    @Column(name = "image_url", length = 1000) // ✅ ADDED: URLs can be long
    private String imageUrl;

    @Column(nullable = false)
    private String source;

    @Column(name = "source_url", columnDefinition = "TEXT") // ✅ CHANGED: URLs can be very long
    private String sourceUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Comma-separated list of related cryptocurrencies
    @Column(name = "related_cryptos", length = 500) // ✅ ADDED: Explicit length
    private String relatedCryptos;

    @Column(length = 100)
    private String category; // NEWS, ANALYSIS, ANNOUNCEMENT, etc.

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}