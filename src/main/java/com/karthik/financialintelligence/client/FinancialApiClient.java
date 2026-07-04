package com.karthik.financialintelligence.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.karthik.financialintelligence.dto.GlobalQuoteResponse;
import com.karthik.financialintelligence.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FinancialApiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public FinancialApiClient(
            RestTemplate restTemplate,
            @Value("${alphavantage.api.key}") String apiKey,
            @Value("${alphavantage.base.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public GlobalQuoteResponse getQuote(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase();
        if (normalizedSymbol.isBlank()) {
            throw new ApiException("Stock symbol must not be blank");
        }

        String requestUrl = UriComponentsBuilder
            .fromUriString(baseUrl)
                .queryParam("function", "GLOBAL_QUOTE")
                .queryParam("symbol", normalizedSymbol)
                .queryParam("apikey", apiKey)
                .toUriString();

        try {
            log.info("Requesting quote from Alpha Vantage for symbol={}", normalizedSymbol);
            GlobalQuoteResponse response = restTemplate.getForObject(requestUrl, GlobalQuoteResponse.class);
            validateQuoteResponse(normalizedSymbol, response);
            return response;
        } catch (RestClientException ex) {
            log.error("Alpha Vantage call failed for symbol={}", normalizedSymbol, ex);
            throw new ApiException("Unable to fetch market data from Alpha Vantage", ex);
        }
    }

    private void validateQuoteResponse(String symbol, GlobalQuoteResponse response) {
        if (response == null || response.getGlobalQuote() == null) {
            throw new ApiException("Empty response from Alpha Vantage for symbol: " + symbol);
        }

        GlobalQuoteResponse.GlobalQuote quote = response.getGlobalQuote();
        if (isBlank(quote.getSymbol()) || isBlank(quote.getPrice())) {
            throw new ApiException("Quote not found or invalid for symbol: " + symbol);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
