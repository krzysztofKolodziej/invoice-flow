package com.example.invoicevalidator.service;

import com.example.invoicevalidator.dto.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvoiceValidatorService {

    private final KafkaTemplate<String, Invoice> kafkaTemplate;
    private static final String INPUT_TOPIC = "invoices-raw";
    private static final String OUTPUT_TOPIC = "invoice-validated";

    @KafkaListener(topics = INPUT_TOPIC, groupId = "validator-group-final-v11")
    public void process(Invoice invoice) {
        log.info("Receive invoice to validation: {}", invoice.invoiceId());

        if (validate(invoice)) {
            log.info("Invoice {} is valid. Routing to {}", invoice.invoiceId(), OUTPUT_TOPIC);
            kafkaTemplate.send(OUTPUT_TOPIC, invoice.invoiceId(), invoice);
        } else {
            log.error("Invoice {} FAILED validation. Dropping message.", invoice.invoiceId());
        }
    }

    private boolean validate(Invoice invoice) {
        if (invoice.lines() == null || invoice.lines().isEmpty()) return false;
        if (invoice.customerId() == null || invoice.customerId().isBlank()) return false;

        return invoice.lines().stream()
                .noneMatch(line -> line.netPrice() < 0 || line.quantity() <= 0);
    }
}
