package com.karthik.financialintelligence.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.financialintelligence.dto.NewsResponse;
import com.karthik.financialintelligence.dto.SentimentResponse;
import com.karthik.financialintelligence.entity.IntelligenceHistory;
import com.karthik.financialintelligence.entity.SentimentHistory;
import com.karthik.financialintelligence.exception.NewsApiException;
import com.karthik.financialintelligence.exception.SentimentAnalysisException;
import com.karthik.financialintelligence.repository.IntelligenceHistoryRepository;
import com.karthik.financialintelligence.repository.SentimentHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SentimentService {

    private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

    private final NewsService newsService;
    private final IntelligenceHistoryRepository intelligenceHistoryRepository;
    private final SentimentHistoryRepository sentimentHistoryRepository;
    private final SentimentAnalysisProvider sentimentAnalysisProvider;
    private final ObjectMapper objectMapper;

    public SentimentResponse generateSentiment(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase();

        List<NewsResponse> newsArticles = fetchNews(normalizedSymbol);
        List<SentimentHistory> previousSentiments = sentimentHistoryRepository
                .findTop5BySymbolOrderByCreatedAtDesc(normalizedSymbol);
        List<IntelligenceHistory> previousIntelligence = intelligenceHistoryRepository
                .findAllByOrderByCreatedAtDesc().stream()
                .filter(item -> normalizedSymbol.equalsIgnoreCase(item.getSymbol()))
                .limit(5)
                .toList();

        String prompt = buildPrompt(normalizedSymbol, newsArticles, previousSentiments, previousIntelligence);
        log.info("Generated sentiment prompt for symbol={}", normalizedSymbol);

        String rawJson = sentimentAnalysisProvider.analyzeSentiment(prompt);
        SentimentPayload payload = parseSentimentJson(rawJson);

        SentimentHistory history = SentimentHistory.builder()
                .symbol(normalizedSymbol)
                .sentiment(payload.sentiment())
                .sentimentScore(payload.sentimentScore())
                .confidence(payload.confidence())
                .summary(payload.summary())
                .keyThemes(toJson(payload.keyThemes()))
                .opportunities(toJson(payload.opportunities()))
                .risks(toJson(payload.risks()))
                .build();

        try {
            SentimentHistory saved = sentimentHistoryRepository.save(history);
            log.info("Saved sentiment history id={} for symbol={}", saved.getId(), normalizedSymbol);
            return toResponse(saved);
        } catch (DataAccessException ex) {
            log.error("Failed to save sentiment history for symbol={}", normalizedSymbol, ex);
            throw new SentimentAnalysisException("Database save failure while storing sentiment history", ex);
        }
    }

    public List<SentimentResponse> getSentimentHistory() {
        return sentimentHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private List<NewsResponse> fetchNews(String symbol) {
        try {
            List<NewsResponse> news = newsService.getNewsBySymbol(symbol);
            log.info("Retrieved {} news articles for symbol={}", news.size(), symbol);
            if (news.isEmpty()) {
                throw new SentimentAnalysisException("No news articles found for symbol: " + symbol);
            }
            return news;
        } catch (NewsApiException ex) {
            log.error("News retrieval failed for symbol={}", symbol, ex);
            throw new SentimentAnalysisException("Unable to retrieve news for sentiment analysis", ex);
        }
    }

    private String buildPrompt(
            String symbol,
            List<NewsResponse> news,
            List<SentimentHistory> previousSentiments,
            List<IntelligenceHistory> previousIntelligence) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a financial sentiment engine. Return ONLY valid JSON.\\n")
                .append("No markdown. No explanations. JSON only.\\n\\n")
                .append("Symbol: ").append(symbol).append("\\n\\n")
                .append("Latest News Articles:\\n");

        news.stream().limit(10).forEach(article -> prompt.append("- ")
                .append(article.getTitle() == null ? "No title" : article.getTitle())
                .append(" | ")
                .append(article.getDescription() == null ? "No description" : article.getDescription())
                .append("\\n"));

        prompt.append("\\nPrevious Sentiment History:\\n");
        if (previousSentiments.isEmpty()) {
            prompt.append("- No previous sentiment records\\n");
        } else {
            previousSentiments.forEach(item -> prompt.append("- Sentiment: ")
                    .append(item.getSentiment())
                    .append(", Score: ")
                    .append(item.getSentimentScore())
                    .append(", Confidence: ")
                    .append(item.getConfidence())
                    .append("\\n"));
        }

        prompt.append("\\nPrevious Intelligence Reports:\\n");
        if (previousIntelligence.isEmpty()) {
            prompt.append("- No previous intelligence reports\\n");
        } else {
            previousIntelligence.forEach(item -> prompt.append("- Sentiment: ")
                    .append(item.getSentiment())
                    .append(", Score: ")
                    .append(item.getIntelligenceScore())
                    .append(", Summary: ")
                    .append(item.getSummary())
                    .append("\\n"));
        }

        prompt.append("\\nProvide JSON with exact keys: sentiment, sentimentScore, confidence, keyThemes, opportunities, risks, summary.\\n")
                .append("Rules:\\n")
                .append("- sentiment: Positive or Neutral or Negative\\n")
                .append("- sentimentScore: integer 0 to 100\\n")
                .append("- confidence: integer 0 to 100\\n")
                .append("- keyThemes: array of strings\\n")
                .append("- opportunities: array of strings\\n")
                .append("- risks: array of strings\\n")
                .append("- summary: concise sentence\\n")
                .append("Output must be JSON only.");

        return prompt.toString();
    }

    private SentimentPayload parseSentimentJson(String rawJson) {
        String cleanedJson = stripMarkdownCodeFence(rawJson);
        try {
            JsonNode root = objectMapper.readTree(cleanedJson);

            String sentiment = requiredText(root, "sentiment");
            int sentimentScore = requiredInt(root, "sentimentScore");
            int confidence = requiredInt(root, "confidence");
            List<String> keyThemes = requiredStringList(root, "keyThemes");
            List<String> opportunities = requiredStringList(root, "opportunities");
            List<String> risks = requiredStringList(root, "risks");
            String summary = requiredText(root, "summary");

            if (sentimentScore < 0 || sentimentScore > 100) {
                throw new SentimentAnalysisException("Invalid sentimentScore range. Expected 0-100.");
            }

            if (confidence < 0 || confidence > 100) {
                throw new SentimentAnalysisException("Invalid confidence range. Expected 0-100.");
            }

            return new SentimentPayload(sentiment, sentimentScore, confidence, keyThemes, opportunities, risks, summary);
        } catch (SentimentAnalysisException ex) {
            log.error("Invalid sentiment JSON response: {}", rawJson, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse sentiment JSON response: {}", rawJson, ex);
            throw new SentimentAnalysisException("Invalid JSON received from sentiment provider", ex);
        }
    }

    private SentimentResponse toResponse(SentimentHistory history) {
        return SentimentResponse.builder()
                .symbol(history.getSymbol())
                .sentiment(history.getSentiment())
                .sentimentScore(history.getSentimentScore())
                .confidence(history.getConfidence())
                .keyThemes(fromJsonList(history.getKeyThemes()))
                .opportunities(fromJsonList(history.getOpportunities()))
                .risks(fromJsonList(history.getRisks()))
                .summary(history.getSummary())
                .build();
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new SentimentAnalysisException("Missing or invalid field in sentiment JSON: " + fieldName);
        }
        return value.asText().trim();
    }

    private int requiredInt(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isInt()) {
            throw new SentimentAnalysisException("Missing or invalid numeric field in sentiment JSON: " + fieldName);
        }
        return value.asInt();
    }

    private List<String> requiredStringList(JsonNode root, String fieldName) {
        JsonNode value = root.path(fieldName);
        if (!value.isArray() || value.isEmpty()) {
            throw new SentimentAnalysisException("Missing or invalid array field in sentiment JSON: " + fieldName);
        }

        List<String> values = new ArrayList<>();
        value.forEach(node -> {
            if (node.isTextual() && !node.asText().isBlank()) {
                values.add(node.asText().trim());
            }
        });

        if (values.isEmpty()) {
            throw new SentimentAnalysisException("Array field contains no valid entries: " + fieldName);
        }
        return values;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new SentimentAnalysisException("Unable to serialize sentiment list data", ex);
        }
    }

    private List<String> fromJsonList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException ex) {
            log.warn("Unable to parse persisted sentiment list value='{}'", value);
            return List.of();
        }
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

    private record SentimentPayload(
            String sentiment,
            int sentimentScore,
            int confidence,
            List<String> keyThemes,
            List<String> opportunities,
            List<String> risks,
            String summary) {
    }
}
