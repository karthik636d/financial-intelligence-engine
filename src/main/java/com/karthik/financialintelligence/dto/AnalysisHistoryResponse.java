package com.karthik.financialintelligence.dto;

import java.time.LocalDateTime;

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
public class AnalysisHistoryResponse {

    private Long id;

    private String symbol;

    private Double currentPrice;

    private String changePercent;

    private String recommendation;

    private LocalDateTime createdAt;
}
