package com.example.invoicevalidator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Config {

    @Bean
    public NewTopic validatedInvoicesTopic() {
        return TopicBuilder.name("invoice-validated")
                .partitions(12)
                .replicas(1)
                .build();
    }
}
