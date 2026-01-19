package com.example.invoicegenerator.factory;

import java.util.List;

public record Invoice(
        String invoiceId,
        String customerId,
        String currency,
        String issuedAt,
        List<InvoiceLine> lines
) {
}
