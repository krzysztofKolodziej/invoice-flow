package com.example.invoicegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Args.class)
public class InvoiceGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceGeneratorApplication.class, args);
    }
}