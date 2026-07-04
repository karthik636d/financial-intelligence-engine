package com.karthik.financialintelligence.exception;

public class NewsApiException extends ApiException {

    public NewsApiException(String message) {
        super(message);
    }

    public NewsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
