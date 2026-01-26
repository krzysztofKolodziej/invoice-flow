package com.example.invoicegenerator.factory;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InvoiceLine(
        @JsonProperty("sku") String sku,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("netPrice") double netPrice,
        @JsonProperty("vatRate") double vatRate
) {
}
