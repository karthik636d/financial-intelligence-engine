package com.karthik.financialintelligence.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.karthik.financialintelligence.client.FinancialNewsClient;
import com.karthik.financialintelligence.dto.NewsApiResponse;
import com.karthik.financialintelligence.dto.NewsResponse;
import com.karthik.financialintelligence.entity.NewsArticle;
import com.karthik.financialintelligence.exception.NewsApiException;
import com.karthik.financialintelligence.repository.NewsArticleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    private final FinancialNewsClient financialNewsClient;
    private final NewsArticleRepository newsArticleRepository;

    public List<NewsResponse> getNewsBySymbol(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase();
        NewsApiResponse apiResponse = financialNewsClient.getNews(normalizedSymbol);

        List<NewsArticle> articles = apiResponse.getArticles().stream()
                .map(article -> NewsArticle.builder()
                        .symbol(normalizedSymbol)
                        .title(article.getTitle())
                        .source(article.getSource() == null ? null : article.getSource().getName())
                        .description(article.getDescription())
                        .url(article.getUrl())
                        .publishedAt(parsePublishedAt(article.getPublishedAt()))
                        .build())
                .toList();

        try {
            List<NewsArticle> savedArticles = newsArticleRepository.saveAll(articles);
            log.info("Saved {} news articles for symbol={}", savedArticles.size(), normalizedSymbol);

            return savedArticles.stream()
                    .map(this::toResponse)
                    .toList();
        } catch (DataAccessException ex) {
            log.error("Failed to save news articles for symbol={}", normalizedSymbol, ex);
            throw new NewsApiException("Database save failure while storing news articles", ex);
        }
    }

    private NewsResponse toResponse(NewsArticle article) {
        return NewsResponse.builder()
                .title(article.getTitle())
                .source(article.getSource())
                .description(article.getDescription())
                .url(article.getUrl())
                .publishedAt(article.getPublishedAt())
                .build();
    }

    private LocalDateTime parsePublishedAt(String publishedAt) {
        if (publishedAt == null || publishedAt.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(publishedAt).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            log.warn("Unable to parse publishedAt value='{}'", publishedAt);
            return null;
        }
    }
}
