package com.karthik.financialintelligence.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.financialintelligence.dto.IntelligenceResponse;
import com.karthik.financialintelligence.dto.NewsResponse;
import com.karthik.financialintelligence.dto.StockQuoteResponse;
import com.karthik.financialintelligence.entity.AnalysisHistory;
import com.karthik.financialintelligence.entity.IntelligenceHistory;
import com.karthik.financialintelligence.exception.GeminiApiException;
import com.karthik.financialintelligence.exception.NewsApiException;
import com.karthik.financialintelligence.repository.AnalysisHistoryRepository;
import com.karthik.financialintelligence.repository.IntelligenceHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceService.class);

    private final MarketDataService marketDataService;
    private final NewsService newsService;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final IntelligenceHistoryRepository intelligenceHistoryRepository;
    private final AIAnalysisProvider aiAnalysisProvider;
    private final ObjectMapper objectMapper;

    public IntelligenceResponse generateIntelligence(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase();

        StockQuoteResponse quote = marketDataService.getStockQuote(normalizedSymbol);
        List<NewsResponse> recentNews = fetchRecentNewsSafely(normalizedSymbol);
        List<AnalysisHistory> analysisHistory = analysisHistoryRepository
            .findTop5BySymbolOrderByCreatedAtDesc(normalizedSymbol);

        String prompt = buildPrompt(normalizedSymbol, quote, recentNews, analysisHistory);
        log.info("Generated intelligence prompt for symbol={}", normalizedSymbol);

        String aiRawJson = aiAnalysisProvider.generateAnalysis(prompt);
        GeminiPayload payload = parseGeminiJson(aiRawJson);

        IntelligenceHistory history = IntelligenceHistory.builder()
                .symbol(quote.getSymbol())
                .currentPrice(quote.getCurrentPrice())
                .changePercent(quote.getChangePercent())
                .sentiment(payload.sentiment())
                .riskLevel(payload.riskLevel())
                .intelligenceScore(payload.intelligenceScore())
                .summary(payload.summary())
                .recommendation(payload.recommendation())
                .build();

        try {
            IntelligenceHistory saved = intelligenceHistoryRepository.save(history);
            log.info("Saved intelligence history record id={} for symbol={}", saved.getId(), normalizedSymbol);
            return toResponse(saved);
        } catch (DataAccessException ex) {
            log.error("Failed to save intelligence history for symbol={}", normalizedSymbol, ex);
            throw new GeminiApiException("Database save failure while storing intelligence history", ex);
        }
    }

    public List<IntelligenceResponse> getIntelligenceHistory() {
        return intelligenceHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private IntelligenceResponse toResponse(IntelligenceHistory history) {
        return IntelligenceResponse.builder()
                .symbol(history.getSymbol())
                .currentPrice(history.getCurrentPrice())
                .changePercent(history.getChangePercent())
                .sentiment(history.getSentiment())
                .riskLevel(history.getRiskLevel())
                .intelligenceScore(history.getIntelligenceScore())
                .summary(history.getSummary())
                .recommendation(history.getRecommendation())
                .build();
    }

    private List<NewsResponse> fetchRecentNewsSafely(String symbol) {
        try {
            return newsService.getNewsBySymbol(symbol);
        } catch (NewsApiException ex) {
            log.warn("News retrieval failed for symbol={}, proceeding with empty news context", symbol, ex);
            return List.of();
        }
    }

    private String buildPrompt(
            String symbol,
            StockQuoteResponse quote,
            List<NewsResponse> news,
            List<AnalysisHistory> analysisHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following stock and return ONLY valid JSON.\\n")
                .append("Do not include markdown, code fences, or extra text.\\n\\n")
                .append("Symbol: ").append(symbol).append("\\n")
                .append("Current Price: ").append(quote.getCurrentPrice()).append("\\n")
                .append("Change Percent: ").append(quote.getChangePercent()).append("\\n\\n")
                .append("Recent News:\\n");

        if (news.isEmpty()) {
            prompt.append("- No recent news available\\n");
        } else {
            news.stream()
                    .limit(5)
                    .forEach(article -> prompt.append("- ")
                            .append(article.getTitle() == null ? "No title" : article.getTitle())
                            .append("\\n"));
        }

        prompt.append("\\nHistorical Analysis:\\n");
        if (analysisHistory.isEmpty()) {
            prompt.append("- No prior analysis available\\n");
        } else {
            analysisHistory.forEach(item -> prompt.append("- Previous recommendation: ")
                    .append(item.getRecommendation())
                    .append("\\n"));
        }

        prompt.append("\\nProvide JSON with exact keys: sentiment, riskLevel, intelligenceScore, summary, recommendation.\\n")
                .append("Rules:\\n")
                .append("- sentiment: one of Positive, Neutral, Negative\\n")
                .append("- riskLevel: one of Low, Medium, High\\n")
                .append("- intelligenceScore: integer from 0 to 100\\n")
                .append("- summary: concise analysis\\n")
                .append("- recommendation: clear investment stance\\n")
                .append("Return JSON only.");

        return prompt.toString();
    }

    private GeminiPayload parseGeminiJson(String rawJson) {
        String cleanedJson = stripMarkdownCodeFence(rawJson);
        try {
            JsonNode root = objectMapper.readTree(cleanedJson);

            String sentiment = requiredText(root, "sentiment");
            String riskLevel = requiredText(root, "riskLevel");
            String summary = requiredText(root, "summary");
            String recommendation = requiredText(root, "recommendation");
            int score = requiredInt(root, "intelligenceScore");

            if (score < 0 || score > 100) {
                throw new GeminiApiException("Invalid intelligenceScore range. Expected 0-100.");
            }

            return new GeminiPayload(sentiment, riskLevel, score, summary, recommendation);
        } catch (GeminiApiException ex) {
            log.error("Invalid Gemini JSON response: {}", rawJson, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse Gemini JSON response: {}", rawJson, ex);
            throw new GeminiApiException("Invalid JSON received from Gemini", ex);
        }
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new GeminiApiException("Missing or invalid field in Gemini JSON: " + fieldName);
        }
        return value.asText().trim();
    }

    private int requiredInt(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isInt()) {
            throw new GeminiApiException("Missing or invalid numeric field in Gemini JSON: " + fieldName);
        }
        return value.asInt();
    }

    private String stripMarkdownCodeFence(String text) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                return trimmed.substring(firstNewline + 1, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private record GeminiPayload(
            String sentiment,
            String riskLevel,
            int intelligenceScore,
            String summary,
            String recommendation) {
    }
}
