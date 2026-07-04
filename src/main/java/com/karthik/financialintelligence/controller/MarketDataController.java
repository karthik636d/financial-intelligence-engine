package com.karthik.financialintelligence.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.StockQuoteResponse;
import com.karthik.financialintelligence.service.MarketDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/{symbol}")
    public ResponseEntity<StockQuoteResponse> getStockQuote(@PathVariable String symbol) {
        StockQuoteResponse response = marketDataService.getStockQuote(symbol);
        return ResponseEntity.ok(response);
    }
}
