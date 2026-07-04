package com.karthik.financialintelligence.exception;

public class GeminiApiException extends ApiException {

    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
