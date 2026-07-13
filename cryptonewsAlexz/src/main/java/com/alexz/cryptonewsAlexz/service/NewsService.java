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
            // Seed mock news if DB is completely empty
            if (newsRepository.count() == 0) {
                log.warn("News DB is empty and no external news fetched — seeding mock news");
                seedMockNewsIfEmpty();
            }
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

    @Transactional
    public void seedMockNewsIfEmpty() {
        List<CryptoNews> mockNews = List.of(
            CryptoNews.builder().newsId("MOCK_001").title("Bitcoin Surges Past $63,000 as Institutional Demand Grows")
                .body("Bitcoin has rallied strongly this week, breaking past the $63,000 level as major institutions continue to accumulate the leading cryptocurrency. Analysts point to growing ETF inflows and macroeconomic tailwinds as key drivers of the rally.")
                .source("CryptoNews").sourceUrl("https://cryptonews.com").publishedAt(LocalDateTime.now().minusHours(1))
                .relatedCryptos("BTC").category("NEWS").build(),
            CryptoNews.builder().newsId("MOCK_002").title("Ethereum Layer-2 Ecosystem Sees Record Transaction Volumes")
                .body("Ethereum layer-2 networks including Arbitrum and Optimism have recorded their highest ever transaction volumes this month, with total value locked crossing $40 billion.")
                .source("Decrypt").sourceUrl("https://decrypt.co").publishedAt(LocalDateTime.now().minusHours(2))
                .relatedCryptos("ETH").category("NEWS").build(),
            CryptoNews.builder().newsId("MOCK_003").title("Solana DeFi Activity Reaches All-Time High in Q3 2024")
                .body("Solana's decentralized finance ecosystem has hit new all-time highs with over $8 billion in TVL and daily DEX volumes surpassing $1.5 billion.")
                .source("CoinDesk").sourceUrl("https://coindesk.com").publishedAt(LocalDateTime.now().minusHours(3))
                .relatedCryptos("SOL").category("NEWS").build(),
            CryptoNews.builder().newsId("MOCK_004").title("SEC Approves New Spot Bitcoin ETFs, Market Reacts Positively")
                .body("The US Securities and Exchange Commission has given the green light to several new spot Bitcoin ETF applications from major asset managers. The approval is expected to bring billions in new institutional capital.")
                .source("The Block").sourceUrl("https://theblock.co").publishedAt(LocalDateTime.now().minusHours(4))
                .relatedCryptos("BTC").category("REGULATION").build(),
            CryptoNews.builder().newsId("MOCK_005").title("Cardano Smart Contract Adoption Grows 300% Year Over Year")
                .body("Cardano has seen a dramatic increase in smart contract activity, with the number of deployed Plutus scripts growing 300% compared to the same period last year.")
                .source("CryptoNews").sourceUrl("https://cryptonews.com").publishedAt(LocalDateTime.now().minusHours(5))
                .relatedCryptos("ADA").category("NEWS").build(),
            CryptoNews.builder().newsId("MOCK_006").title("Polkadot Parachain Auction Results in Record Bids")
                .body("The latest Polkadot parachain auction concluded with record-breaking DOT bids, signaling strong developer and community confidence in the multi-chain ecosystem.")
                .source("Decrypt").sourceUrl("https://decrypt.co").publishedAt(LocalDateTime.now().minusHours(6))
                .relatedCryptos("DOT").category("NEWS").build(),
            CryptoNews.builder().newsId("MOCK_007").title("Global Crypto Market Cap Approaches $2.5 Trillion Milestone")
                .body("The total cryptocurrency market capitalization is approaching the $2.5 trillion mark as Bitcoin and altcoins continue their upward trajectory.")
                .source("CoinMarketCap").sourceUrl("https://coinmarketcap.com").publishedAt(LocalDateTime.now().minusHours(7))
                .relatedCryptos("BTC,ETH").category("MARKET").build(),
            CryptoNews.builder().newsId("MOCK_008").title("DeFi Protocol Aave Launches New Risk Management Features")
                .body("Leading decentralized lending protocol Aave has introduced advanced risk management features including dynamic liquidation thresholds and improved oracle integrations.")
                .source("The Block").sourceUrl("https://theblock.co").publishedAt(LocalDateTime.now().minusHours(8))
                .relatedCryptos("ETH").category("DEFI").build(),
            CryptoNews.builder().newsId("MOCK_009").title("Crypto Adoption in Emerging Markets Surges as Dollar Weakens")
                .body("Cryptocurrency adoption in emerging markets including Latin America, Africa, and Southeast Asia continues to accelerate as local currencies face inflation pressures.")
                .source("CoinDesk").sourceUrl("https://coindesk.com").publishedAt(LocalDateTime.now().minusHours(10))
                .relatedCryptos("BTC").category("ADOPTION").build(),
            CryptoNews.builder().newsId("MOCK_010").title("NFT Market Shows Signs of Recovery with Blue-Chip Collections Rising")
                .body("The NFT market is showing signs of recovery after months of decline, with blue-chip collections like CryptoPunks and Bored Ape Yacht Club seeing significant price increases.")
                .source("NFT Plazas").sourceUrl("https://nftplazas.com").publishedAt(LocalDateTime.now().minusHours(12))
                .relatedCryptos("ETH").category("NFT").build()
        );

        List<CryptoNews> toSave = mockNews.stream()
                .filter(n -> !newsRepository.existsByNewsId(n.getNewsId()))
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            List<CryptoNews> saved = newsRepository.saveAll(toSave);
            log.info("Seeded {} mock news articles", saved.size());
            List<NewsDTO> dtos = saved.stream().map(this::convertToDTO).collect(Collectors.toList());
            webSocketService.broadcastNewsBatch(dtos);
        }
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
