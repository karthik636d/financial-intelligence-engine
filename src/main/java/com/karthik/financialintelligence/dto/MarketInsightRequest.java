package com.karthik.financialintelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketInsightRequest {

    private String symbol;

    private String companyName;

    private Double currentPrice;

    private String aiSummary;
}
