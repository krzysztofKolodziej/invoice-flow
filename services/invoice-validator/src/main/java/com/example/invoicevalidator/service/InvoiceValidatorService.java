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

    /**
     * Processes and validates incoming invoices from the Kafka topic.
     * <p>
     * NOTE: This method contains a deliberate {@code Thread.sleep(5)} to simulate
     * processing latency. This is used specifically for load testing and
     * demonstrating "Consumer Lag" in monitoring tools like Grafana.
     * </p>
     * <p>
     * Without this artificial delay, the validator processes messages faster
     * than the generator can produce them, resulting in a constant zero lag.
     * </p>
     *
     * @param invoice the invoice data received from the input topic
     * @throws InterruptedException if the simulated delay is interrupted
     */
    @KafkaListener(topics = INPUT_TOPIC, groupId = "validator-group-final-v11")
    public void process(Invoice invoice) throws InterruptedException {
        Thread.sleep(5);
        if (validate(invoice)) {
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
