package com.karthik.financialintelligence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.IntelligenceResponse;
import com.karthik.financialintelligence.service.IntelligenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final IntelligenceService intelligenceService;

    @GetMapping("/{symbol}")
    public IntelligenceResponse generateIntelligence(@PathVariable String symbol) {
        return intelligenceService.generateIntelligence(symbol);
    }

    @GetMapping("/history")
    public List<IntelligenceResponse> getIntelligenceHistory() {
        return intelligenceService.getIntelligenceHistory();
    }
}
