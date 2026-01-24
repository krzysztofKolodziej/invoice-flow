package com.example.invoiceapi.dto;

public record InvoiceLine(
        String sku,
        int quantity,
        double netPrice,
        double vatRate
) {}
