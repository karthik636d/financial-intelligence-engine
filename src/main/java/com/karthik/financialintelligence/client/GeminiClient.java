package com.karthik.financialintelligence.client;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthik.financialintelligence.exception.GeminiApiException;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public String generateFinancialAnalysis(String prompt) {
        if ("LOCAL_MOCK".equalsIgnoreCase(apiKey)) {
            log.info("Using LOCAL_MOCK Gemini response");
            return "{\"sentiment\":\"Positive\",\"riskLevel\":\"Medium\",\"intelligenceScore\":82,\"summary\":\"Company outlook appears constructive based on available market and news context.\",\"recommendation\":\"Bullish long-term outlook. Monitor earnings and AI-related investments.\"}";
        }

        if (apiKey == null || apiKey.isBlank() || "YOUR_GEMINI_API_KEY".equals(apiKey)) {
            throw new GeminiApiException("Gemini API key is missing or not configured");
        }

        String requestUrl = UriComponentsBuilder
                .fromUriString("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
                .queryParam("key", apiKey)
                .toUriString();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Sending Gemini analysis request");
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class);

            String content = extractTextContent(response.getBody());
            if (content == null || content.isBlank()) {
                throw new GeminiApiException("Empty Gemini response content");
            }

            log.info("Received Gemini analysis response");
            return content;
        } catch (HttpStatusCodeException ex) {
            log.error("Gemini API HTTP error status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new GeminiApiException("Invalid Gemini API key", ex);
            }
            throw new GeminiApiException("Gemini API unavailable", ex);
        } catch (ResourceAccessException ex) {
            log.error("Gemini API timeout/network error", ex);
            throw new GeminiApiException("Network timeout while calling Gemini API", ex);
        } catch (RestClientException ex) {
            log.error("Gemini API call failed", ex);
            throw new GeminiApiException("Failed to call Gemini API", ex);
        }
    }

    private String extractTextContent(String rawResponseBody) {
        try {
            JsonNode root = objectMapper.readTree(rawResponseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return null;
            }

            JsonNode textNode = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");
            return textNode.isMissingNode() ? null : textNode.asText();
        } catch (Exception ex) {
            throw new GeminiApiException("Unable to parse Gemini API response", ex);
        }
    }
}
