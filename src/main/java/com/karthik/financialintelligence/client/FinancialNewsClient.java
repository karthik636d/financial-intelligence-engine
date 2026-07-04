package com.karthik.financialintelligence.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.karthik.financialintelligence.dto.NewsApiResponse;
import com.karthik.financialintelligence.exception.NewsApiException;

@Component
public class FinancialNewsClient {

    private static final Logger log = LoggerFactory.getLogger(FinancialNewsClient.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public FinancialNewsClient(
            RestTemplate restTemplate,
            @Value("${news.api.key}") String apiKey,
            @Value("${news.base.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public NewsApiResponse getNews(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase();
        if (normalizedSymbol.isBlank()) {
            throw new NewsApiException("Stock symbol must not be blank");
        }

        if ("LOCAL_MOCK".equalsIgnoreCase(apiKey)) {
            log.info("Using LOCAL_MOCK news response for symbol={}", normalizedSymbol);
            return NewsApiResponse.builder()
                    .status("ok")
                    .articles(List.of(
                            NewsApiResponse.Article.builder()
                                    .title(normalizedSymbol + " expands AI infrastructure and cloud footprint")
                                    .description("Mock news used for local development and integration validation.")
                                    .url("https://example.com/mock-news/" + normalizedSymbol.toLowerCase())
                                    .publishedAt("2026-07-04T10:00:00Z")
                                    .source(NewsApiResponse.Source.builder().name("Mock Financial Wire").build())
                                    .build()))
                    .build();
        }

        if (apiKey == null || apiKey.isBlank() || "YOUR_NEWS_API_KEY".equals(apiKey)) {
            throw new NewsApiException("News API key is missing or not configured");
        }

        String requestUrl = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/everything")
                .queryParam("q", normalizedSymbol)
                .queryParam("sortBy", "publishedAt")
                .queryParam("language", "en")
                .queryParam("pageSize", 10)
                .queryParam("apiKey", apiKey)
                .toUriString();

        try {
            log.info("Requesting news from NewsAPI for symbol={}", normalizedSymbol);
            NewsApiResponse response = restTemplate.getForObject(requestUrl, NewsApiResponse.class);
            validateResponse(normalizedSymbol, response);
            log.info("Received {} news articles for symbol={}", response.getArticles().size(), normalizedSymbol);
            return response;
        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String responseBody = ex.getResponseBodyAsString();
            log.error("NewsAPI HTTP error for symbol={} status={} body={}", normalizedSymbol, statusCode, responseBody, ex);
            if (statusCode.value() == 401 || responseBody.contains("apiKeyInvalid") || responseBody.contains("apiKeyMissing")) {
                throw new NewsApiException("Invalid News API key", ex);
            }
            throw new NewsApiException("News API unavailable for symbol: " + normalizedSymbol, ex);
        } catch (ResourceAccessException ex) {
            log.error("NewsAPI timeout/network error for symbol={}", normalizedSymbol, ex);
            throw new NewsApiException("Network timeout while calling News API", ex);
        } catch (RestClientException ex) {
            log.error("NewsAPI client error for symbol={}", normalizedSymbol, ex);
            throw new NewsApiException("Failed to fetch news from News API", ex);
        }
    }

    private void validateResponse(String symbol, NewsApiResponse response) {
        if (response == null) {
            throw new NewsApiException("Empty response from News API for symbol: " + symbol);
        }

        if (!"ok".equalsIgnoreCase(response.getStatus())) {
            String errorMessage = response.getMessage() == null ? "Unknown News API error" : response.getMessage();
            throw new NewsApiException("News API error: " + errorMessage);
        }

        if (response.getArticles() == null || response.getArticles().isEmpty()) {
            throw new NewsApiException("No news articles returned for symbol: " + symbol);
        }
    }
}
