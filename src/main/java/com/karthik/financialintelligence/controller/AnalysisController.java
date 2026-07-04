package com.karthik.financialintelligence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.AnalysisHistoryResponse;
import com.karthik.financialintelligence.dto.AnalysisResponse;
import com.karthik.financialintelligence.service.AnalysisService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/history")
    public List<AnalysisHistoryResponse> getHistory() {
        return analysisService.getAnalysisHistory();
    }

    @GetMapping("/{symbol}")
    public AnalysisResponse analyzeSymbol(@PathVariable String symbol) {
        return analysisService.analyzeStock(symbol);
    }
}
