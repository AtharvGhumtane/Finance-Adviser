package com.alexz.cryptonewsAlexz.service;

import com.alexz.cryptonewsAlexz.dto.CryptoPriceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CryptoPriceDTO> priceCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Configured coins (with name and symbol)
    private static final List<Map<String, String>> COIN_CONFIG = List.of(
            Map.of("id", "bitcoin", "symbol", "BTC", "name", "Bitcoin", "icon", "₿"),
            Map.of("id", "ethereum", "symbol", "ETH", "name", "Ethereum", "icon", "Ξ"),
            Map.of("id", "solana", "symbol", "SOL", "name", "Solana", "icon", "☀️"),
            Map.of("id", "cardano", "symbol", "ADA", "name", "Cardano", "icon", "₳"),
            Map.of("id", "polkadot", "symbol", "DOT", "name", "Polkadot", "icon", "●")
    );

    // Initial fallback prices
    private static final Map<String, Double[]> FALLBACK_PRICES = Map.of(
            "BTC", new Double[]{65432.10, 1.25},
            "ETH", new Double[]{3456.78, -0.45},
            "SOL", new Double[]{145.60, 4.82},
            "ADA", new Double[]{0.485, -2.15},
            "DOT", new Double[]{6.25, 0.78}
    );

    public Collection<CryptoPriceDTO> getAllPrices() {
        if (priceCache.isEmpty()) {
            initializeFallbackPrices();
        }
        return priceCache.values();
    }

    private void initializeFallbackPrices() {
        for (Map<String, String> config : COIN_CONFIG) {
            String symbol = config.get("symbol");
            Double[] priceInfo = FALLBACK_PRICES.get(symbol);
            priceCache.put(symbol, CryptoPriceDTO.builder()
                    .symbol(symbol)
                    .name(config.get("name"))
                    .price(priceInfo[0])
                    .priceChangePercentage24h(priceInfo[1])
                    .icon(config.get("icon"))
                    .build());
        }
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void fetchPrices() {
        try {
            log.debug("Fetching live crypto prices from CoinGecko...");
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,cardano,polkadot&vs_currencies=usd&include_24hr_change=true";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            for (Map<String, String> config : COIN_CONFIG) {
                String id = config.get("id");
                String symbol = config.get("symbol");
                JsonNode coinNode = root.path(id);

                if (!coinNode.isMissingNode() && !coinNode.isNull()) {
                    double price = coinNode.path("usd").asDouble();
                    double change = coinNode.path("usd_24h_change").asDouble();

                    CryptoPriceDTO updatedPrice = CryptoPriceDTO.builder()
                            .symbol(symbol)
                            .name(config.get("name"))
                            .price(price)
                            .priceChangePercentage24h(change)
                            .icon(config.get("icon"))
                            .build();

                    priceCache.put(symbol, updatedPrice);
                    webSocketService.broadcastPrice(updatedPrice);
                }
            }
            log.debug("Live crypto prices updated successfully");
        } catch (Exception e) {
            log.warn("Failed to fetch live prices from API, updating cache with simulated changes: {}", e.getMessage());
            updateSimulatedPrices();
        }
    }

    private void updateSimulatedPrices() {
        if (priceCache.isEmpty()) {
            initializeFallbackPrices();
        }
        for (String symbol : priceCache.keySet()) {
            CryptoPriceDTO cached = priceCache.get(symbol);
            // Simulate minor fluctuation (random walk)
            double pct = (random.nextDouble() - 0.5) * 0.002; // max 0.1% change
            double newPrice = cached.getPrice() * (1 + pct);
            double newChange = cached.getPriceChangePercentage24h() + (random.nextDouble() - 0.5) * 0.1;

            CryptoPriceDTO updated = CryptoPriceDTO.builder()
                    .symbol(symbol)
                    .name(cached.getName())
                    .price(newPrice)
                    .priceChangePercentage24h(newChange)
                    .icon(cached.getIcon())
                    .build();

            priceCache.put(symbol, updated);
            webSocketService.broadcastPrice(updated);
        }
    }
}
