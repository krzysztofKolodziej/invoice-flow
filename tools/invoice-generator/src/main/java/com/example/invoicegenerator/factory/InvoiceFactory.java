package com.example.invoicegenerator.factory;

import com.example.invoicegenerator.GeneratorProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class InvoiceFactory {

    private final Random rnd = new Random();
    private final ObjectMapper om;
    private final GeneratorProperties gp;

    private static final List<Double> VAT_RATES = List.of(0.23, 0.08, 0.05, 0.00);

    public JsonNode newInvoice() {
        Invoice invoice = new Invoice(
                "inv_" + UUID.randomUUID(),
                "cus_" + UUID.randomUUID(),
                "PLN",
                Instant.now().toString(),
                generateLines()
        );

        return om.valueToTree(invoice);
    }

    private List<InvoiceLine> generateLines() {
        int lineCount = Math.max(1, (int) Math.round(gp.getAvgLines() + rnd.nextGaussian()));
        return Stream.generate(this::generateSingleLine)
                .limit(lineCount)
                .toList();
    }

    private InvoiceLine generateSingleLine() {
        return new InvoiceLine(
                "SKU-" + (rnd.nextInt(1000) + 1),
                rnd.nextInt(5) + 1,
                (rnd.nextInt(50000) + 100) / 100.0,
                VAT_RATES.get(rnd.nextInt(VAT_RATES.size()))
        );
    }
}

