package com.example.invoicegenerator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class InvoiceSender {

    private final RestClient restClient;

    /**
     * Transmits an invoice payload to the configured endpoint.
     * <p>
     * <b>Error Handling Strategy:</b>
     * <ul>
     * <li>HTTP 4xx/5xx: Logged as warnings, returns the actual status code.</li>
     * <li>Network Exceptions: Logged as errors, returns {@code 0} to indicate a transport failure.</li>
     * </ul>
     *
     * @param invoice the JSON representation of the invoice to send
     * @return the HTTP status code, or 0 if a network exception occurred
     */
    public int send(JsonNode invoice) {
        try {
            int code = restClient.post()
                    .uri("/v1/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invoice)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {})
                    .toBodilessEntity()
                    .getStatusCode()
                    .value();

            ResponseClassifier.logResponse(code);
            return code;
        } catch (Exception e) {
            ResponseClassifier.logResponse(0);
            return 0;
        }
    }
}
