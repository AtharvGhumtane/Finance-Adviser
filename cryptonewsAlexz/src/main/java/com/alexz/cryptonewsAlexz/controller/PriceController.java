package com.alexz.cryptonewsAlexz.controller;

import com.alexz.cryptonewsAlexz.dto.CryptoPriceDTO;
import com.alexz.cryptonewsAlexz.service.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PriceController {

    private final PriceService priceService;

    /**
     * Get live prices for all supported cryptocurrencies
     * GET /api/prices/all
     */
    @GetMapping("/all")
    public ResponseEntity<Collection<CryptoPriceDTO>> getAllPrices() {
        log.info("Fetching all crypto prices");
        return ResponseEntity.ok(priceService.getAllPrices());
    }
}
