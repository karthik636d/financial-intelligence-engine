package com.karthik.financialintelligence.service;

import org.springframework.stereotype.Service;

@Service
public class RuleBasedRecommendationEngine implements RecommendationEngine {

    @Override
    public String generateRecommendation(String changePercent) {
        double value = parseChangePercent(changePercent);

        if (value > 2.0) {
            return "Bullish momentum detected. Consider monitoring for short-term opportunities.";
        }

        if (value < -2.0) {
            return "Bearish trend detected. Exercise caution before investing.";
        }

        return "Market appears stable. Hold and observe.";
    }

    private double parseChangePercent(String changePercent) {
        if (changePercent == null || changePercent.isBlank()) {
            return 0.0;
        }

        String normalized = changePercent.replace("%", "").trim();
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
}
