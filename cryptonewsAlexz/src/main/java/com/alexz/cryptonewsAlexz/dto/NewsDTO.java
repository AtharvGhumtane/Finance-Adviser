package com.alexz.cryptonewsAlexz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String newsId;
    private String title;
    private String body;
    private String imageUrl;
    private String source;
    private String sourceUrl;
    private LocalDateTime publishedAt;
    private String relatedCryptos;
    private String category;
    private String timeAgo; // "5 minutes ago"
}

