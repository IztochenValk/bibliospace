package com.chague.bibliotheque.api.exception.advice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiErrorFactory {

    private ApiErrorFactory() {
    }

    public static Map<String, Object> build(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("fieldErrors", null);
        return body;
    }
}
