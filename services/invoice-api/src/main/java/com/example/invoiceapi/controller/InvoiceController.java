package com.example.invoiceapi.controller;

import com.example.invoiceapi.dto.Invoice;
import com.example.invoiceapi.service.InvoiceIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
@RestController
public class InvoiceController {

    private final InvoiceIngestionService ingestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receiveInvoice(@RequestBody Invoice invoice) {
        ingestionService.ingest(invoice);
    }
}
