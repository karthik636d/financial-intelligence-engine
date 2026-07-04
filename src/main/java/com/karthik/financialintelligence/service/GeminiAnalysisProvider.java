package com.karthik.financialintelligence.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.karthik.financialintelligence.client.GeminiClient;
import com.karthik.financialintelligence.exception.GeminiApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiAnalysisProvider implements AIAnalysisProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAnalysisProvider.class);

    private final GeminiClient geminiClient;

    @Override
    public String generateAnalysis(String prompt) {
        try {
            return geminiClient.generateFinancialAnalysis(prompt);
        } catch (GeminiApiException ex) {
            log.error("Gemini analysis generation failed", ex);
            throw ex;
        }
    }
}
