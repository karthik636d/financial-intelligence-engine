package com.karthik.financialintelligence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.karthik.financialintelligence.entity.IntelligenceHistory;

@Repository
public interface IntelligenceHistoryRepository extends JpaRepository<IntelligenceHistory, Long> {

    List<IntelligenceHistory> findAllByOrderByCreatedAtDesc();
}
