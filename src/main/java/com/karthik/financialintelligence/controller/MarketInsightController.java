package com.karthik.financialintelligence.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.MarketInsightRequest;
import com.karthik.financialintelligence.dto.MarketInsightResponse;
import com.karthik.financialintelligence.service.MarketInsightService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class MarketInsightController {

    private final MarketInsightService marketInsightService;

    @GetMapping
    public List<MarketInsightResponse> getAllInsights() {
        return marketInsightService.getAllInsights();
    }

    @GetMapping("/{id}")
    public MarketInsightResponse getInsightById(@PathVariable Long id) {
        return marketInsightService.getInsightById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarketInsightResponse saveInsight(@RequestBody MarketInsightRequest request) {
        return marketInsightService.saveInsight(request);
    }
}
