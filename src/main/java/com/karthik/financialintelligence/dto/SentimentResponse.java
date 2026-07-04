package com.karthik.financialintelligence.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentResponse {

    private String symbol;

    private String sentiment;

    private Integer sentimentScore;

    private Integer confidence;

    private List<String> keyThemes;

    private List<String> opportunities;

    private List<String> risks;

    private String summary;
}
