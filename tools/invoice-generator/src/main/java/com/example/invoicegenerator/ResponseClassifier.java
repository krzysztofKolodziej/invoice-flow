package com.example.invoicegenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseClassifier {

    public static void logResponse(int statusCode) {
        String bucket = mapToStatusBucket(statusCode);

        switch (bucket) {
            case "2xx" -> log.debug("Request successful: {}", statusCode);
            case "4xx" -> log.warn("Client-side error: {}", statusCode);
            case "5xx" -> log.error("Server-side error: {}", statusCode);
            case "network_error" -> log.error("Request failed: Transport/Network level exception");
            default -> log.info("Request finished with status: {}", statusCode);
        }
    }

    public static String mapToStatusBucket(int statusCode) {
        if (statusCode == 0) return "network_error";

        return switch (statusCode / 100) {
            case 2 -> "2xx";
            case 4 -> "4xx";
            case 5 -> "5xx";
            default -> "unknown";
        };
    }
}