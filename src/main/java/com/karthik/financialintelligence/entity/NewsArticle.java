package com.karthik.financialintelligence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "news_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    @Column(columnDefinition = "TEXT")
    private String title;

    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String url;

    private LocalDateTime publishedAt;

    private LocalDateTime fetchedAt;

    @PrePersist
    void onCreate() {
        this.fetchedAt = LocalDateTime.now();
    }
}
