package com.example.invoiceapi.service;

import com.example.invoiceapi.dto.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvoiceIngestionService {

    private final KafkaTemplate<String, Invoice> kafkaTemplate;
    private static final String TOPIC = "invoices-raw";

    public void ingest(Invoice invoice) {
        log.debug("Ingesting invoice: {}", invoice.invoiceId());

        kafkaTemplate.send(TOPIC, invoice.invoiceId(), invoice)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.trace("Invoice {} sent to Kafka", invoice.invoiceId());
                    } else {
                        log.error("Failed to send invoice to Kafka", ex);
                    }
                });
    }
}
