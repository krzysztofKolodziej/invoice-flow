package com.example.invoiceapi.dto;

import java.util.List;

public record Invoice(
        String id,
        String customerId,
        String currency,
        String createdAt,
        List<InvoiceLine> lines
) {}

