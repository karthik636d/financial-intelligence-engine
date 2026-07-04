package com.karthik.financialintelligence.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karthik.financialintelligence.dto.NewsResponse;
import com.karthik.financialintelligence.service.NewsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/{symbol}")
    public List<NewsResponse> getNewsBySymbol(@PathVariable String symbol) {
        return newsService.getNewsBySymbol(symbol);
    }
}
