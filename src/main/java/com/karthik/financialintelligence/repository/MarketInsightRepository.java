package com.karthik.financialintelligence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.karthik.financialintelligence.entity.MarketInsight;

@Repository
public interface MarketInsightRepository extends JpaRepository<MarketInsight, Long> {
}
