package com.karthik.financialintelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.karthik.financialintelligence.entity.AnalysisHistory;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {

	List<AnalysisHistory> findTop5BySymbolOrderByCreatedAtDesc(String symbol);
}
