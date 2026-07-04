package com.karthik.financialintelligence.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.karthik.financialintelligence.dto.AnalysisHistoryResponse;
import com.karthik.financialintelligence.dto.AnalysisResponse;
import com.karthik.financialintelligence.dto.StockQuoteResponse;
import com.karthik.financialintelligence.entity.AnalysisHistory;
import com.karthik.financialintelligence.repository.AnalysisHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final MarketDataService marketDataService;
    private final RecommendationEngine recommendationEngine;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    public AnalysisResponse analyzeStock(String symbol) {
        StockQuoteResponse quote = marketDataService.getStockQuote(symbol);
        String recommendation = recommendationEngine.generateRecommendation(quote.getChangePercent());

        AnalysisHistory analysisHistory = AnalysisHistory.builder()
                .symbol(quote.getSymbol())
                .currentPrice(quote.getCurrentPrice())
                .changePercent(quote.getChangePercent())
                .recommendation(recommendation)
                .build();

        AnalysisHistory saved = analysisHistoryRepository.save(analysisHistory);
        return toAnalysisResponse(saved);
    }

    public List<AnalysisHistoryResponse> getAnalysisHistory() {
        return analysisHistoryRepository.findAll()
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AnalysisResponse toAnalysisResponse(AnalysisHistory analysis) {
        return AnalysisResponse.builder()
                .symbol(analysis.getSymbol())
                .currentPrice(analysis.getCurrentPrice())
                .changePercent(analysis.getChangePercent())
                .recommendation(analysis.getRecommendation())
                .build();
    }

    private AnalysisHistoryResponse toHistoryResponse(AnalysisHistory analysis) {
        return AnalysisHistoryResponse.builder()
                .id(analysis.getId())
                .symbol(analysis.getSymbol())
                .currentPrice(analysis.getCurrentPrice())
                .changePercent(analysis.getChangePercent())
                .recommendation(analysis.getRecommendation())
                .createdAt(analysis.getCreatedAt())
                .build();
    }
}
