package com.example.invoicegenerator.factory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Invoice(
        @JsonProperty("invoiceId") String invoiceId,
        @JsonProperty("customerId") String customerId,
        @JsonProperty("currency") String currency,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("lines") List<InvoiceLine> lines
) {
}
