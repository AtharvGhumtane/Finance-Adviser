package com.alexz.cryptonewsAlexz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoPriceDTO {
    private String symbol;
    private String name;
    private Double price;
    private Double priceChangePercentage24h;
    private String icon;
}
