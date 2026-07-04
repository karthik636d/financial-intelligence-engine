package com.karthik.financialintelligence.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.karthik.financialintelligence.dto.MarketInsightRequest;
import com.karthik.financialintelligence.dto.MarketInsightResponse;
import com.karthik.financialintelligence.entity.MarketInsight;
import com.karthik.financialintelligence.repository.MarketInsightRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketInsightService {

    private final MarketInsightRepository marketInsightRepository;

    public MarketInsightResponse saveInsight(MarketInsightRequest request) {
        MarketInsight insight = MarketInsight.builder()
                .symbol(request.getSymbol())
                .companyName(request.getCompanyName())
                .currentPrice(request.getCurrentPrice())
                .aiSummary(request.getAiSummary())
                .build();

        MarketInsight saved = marketInsightRepository.save(insight);
        return toResponse(saved);
    }

    public List<MarketInsightResponse> getAllInsights() {
        return marketInsightRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MarketInsightResponse getInsightById(Long id) {
        MarketInsight insight = marketInsightRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insight not found: " + id));
        return toResponse(insight);
    }

    private MarketInsightResponse toResponse(MarketInsight insight) {
        return MarketInsightResponse.builder()
                .id(insight.getId())
                .symbol(insight.getSymbol())
                .companyName(insight.getCompanyName())
                .currentPrice(insight.getCurrentPrice())
                .aiSummary(insight.getAiSummary())
                .createdAt(insight.getCreatedAt())
                .build();
    }
}
