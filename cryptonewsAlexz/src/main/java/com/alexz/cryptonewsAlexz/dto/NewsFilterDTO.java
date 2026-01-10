package com.alexz.cryptonewsAlexz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsFilterDTO {
    private String cryptocurrency; // BTC, ETH, etc.
    private String category;
    private Integer limit;
    private Integer page;
}
