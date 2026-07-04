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
public class AnalysisResponse {

    private String symbol;

    private Double currentPrice;

    private String changePercent;

    private String recommendation;
}
