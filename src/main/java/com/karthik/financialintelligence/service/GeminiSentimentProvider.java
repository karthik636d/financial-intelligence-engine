package com.karthik.financialintelligence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karthik.financialintelligence.client.GeminiClient;
import com.karthik.financialintelligence.exception.SentimentAnalysisException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiSentimentProvider implements SentimentAnalysisProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiSentimentProvider.class);

    private final GeminiClient geminiClient;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Override
    public String analyzeSentiment(String prompt) {
        if ("LOCAL_MOCK".equalsIgnoreCase(geminiApiKey)) {
            log.info("Using LOCAL_MOCK sentiment response");
            return "{\"sentiment\":\"Positive\",\"sentimentScore\":78,\"confidence\":85,\"keyThemes\":[\"Artificial Intelligence\",\"Cloud Growth\",\"Enterprise Adoption\"],\"opportunities\":[\"Azure expansion\",\"AI investments\"],\"risks\":[\"Competition\",\"Regulatory pressure\"],\"summary\":\"Recent news coverage is strongly positive with focus on AI and cloud growth.\"}";
        }

        try {
            return geminiClient.generateFinancialAnalysis(prompt);
        } catch (Exception ex) {
            log.error("Gemini sentiment analysis failed", ex);
            throw new SentimentAnalysisException("Gemini sentiment analysis failed", ex);
        }
    }
}
