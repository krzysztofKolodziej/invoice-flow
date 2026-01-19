package com.example.invoicegenerator.factory;

public record InvoiceLine(
        String sku,
        int qty,
        double net,
        double vatRate
) {
}
