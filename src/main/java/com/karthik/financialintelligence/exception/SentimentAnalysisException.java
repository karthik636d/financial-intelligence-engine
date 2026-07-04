package com.karthik.financialintelligence.exception;

public class SentimentAnalysisException extends ApiException {

    public SentimentAnalysisException(String message) {
        super(message);
    }

    public SentimentAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
