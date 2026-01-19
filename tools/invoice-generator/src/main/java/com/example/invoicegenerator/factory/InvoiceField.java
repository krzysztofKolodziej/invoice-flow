package com.example.invoicegenerator.factory;

public enum InvoiceField {
    INVOICE_ID("invoiceId"),
    CUSTOMER_ID("customerId"),
    LINES("lines");

    public final String key;

    InvoiceField(String key) {
        this.key = key;
    }
}
