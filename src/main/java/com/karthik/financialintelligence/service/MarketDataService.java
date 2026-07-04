package com.karthik.financialintelligence.service;

import org.springframework.stereotype.Service;

import com.karthik.financialintelligence.client.FinancialApiClient;
import com.karthik.financialintelligence.dto.GlobalQuoteResponse;
import com.karthik.financialintelligence.dto.StockQuoteResponse;
import com.karthik.financialintelligence.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final FinancialApiClient financialApiClient;

    public StockQuoteResponse getStockQuote(String symbol) {
        GlobalQuoteResponse response = financialApiClient.getQuote(symbol);
        GlobalQuoteResponse.GlobalQuote quote = response.getGlobalQuote();

        String resolvedSymbol = quote.getSymbol();
        String priceRaw = quote.getPrice();
        String changePercent = quote.getChangePercent();

        if (resolvedSymbol == null || resolvedSymbol.isBlank()) {
            throw new ApiException("Invalid symbol in market quote response");
        }

        if (priceRaw == null || priceRaw.isBlank()) {
            throw new ApiException("Missing price in market quote response for symbol: " + resolvedSymbol);
        }

        Double currentPrice;
        try {
            currentPrice = Double.valueOf(priceRaw.trim());
        } catch (NumberFormatException ex) {
            log.error("Unable to parse price '{}' for symbol={}", priceRaw, resolvedSymbol, ex);
            throw new ApiException("Invalid price format in market quote response for symbol: " + resolvedSymbol, ex);
        }

        return StockQuoteResponse.builder()
                .symbol(resolvedSymbol)
                .currentPrice(currentPrice)
                .changePercent(changePercent)
                .build();
    }
}
