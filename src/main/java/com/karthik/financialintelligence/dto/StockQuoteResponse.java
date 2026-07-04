package com.karthik.financialintelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteResponse {

    private String symbol;

    private Double currentPrice;

    private String changePercent;
}
