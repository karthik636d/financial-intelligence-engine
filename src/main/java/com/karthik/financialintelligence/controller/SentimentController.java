package com.karthik.financialintelligence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.SentimentResponse;
import com.karthik.financialintelligence.service.SentimentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sentiment")
@RequiredArgsConstructor
public class SentimentController {

    private final SentimentService sentimentService;

    @GetMapping("/{symbol}")
    public SentimentResponse generateSentiment(@PathVariable String symbol) {
        return sentimentService.generateSentiment(symbol);
    }

    @GetMapping("/history")
    public List<SentimentResponse> getSentimentHistory() {
        return sentimentService.getSentimentHistory();
    }
}
