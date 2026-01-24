package com.example.invoiceapi.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class Config {

    @Bean
    public NewTopic invoicesRaw() {
        return TopicBuilder.name("invoices-raw")
                .partitions(12)
                .replicas(1)
                .build();
    }
}
