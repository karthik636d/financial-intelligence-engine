package com.karthik.financialintelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.karthik.financialintelligence.entity.SentimentHistory;

@Repository
public interface SentimentHistoryRepository extends JpaRepository<SentimentHistory, Long> {

    List<SentimentHistory> findAllByOrderByCreatedAtDesc();

    List<SentimentHistory> findTop5BySymbolOrderByCreatedAtDesc(String symbol);
}
