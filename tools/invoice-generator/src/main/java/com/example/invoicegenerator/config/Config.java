package com.example.invoicegenerator.config;

import com.example.invoicegenerator.GeneratorProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.NonNull;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

@Configuration
public class Config {

    public static final String INVOICE_TIMER_NAME = "invoice.requests";

    @Bean
    public MeterRegistryCustomizer<@NonNull MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().meterFilter(new MeterFilter() {
            @Override
            @NonNull
            public DistributionStatisticConfig configure(@NonNull Meter.Id id, @NonNull DistributionStatisticConfig config) {
                if (id.getName().equals(INVOICE_TIMER_NAME)) {
                    return DistributionStatisticConfig.builder()
                            .percentiles(0.5, 0.95, 0.99)
                            .percentilesHistogram(true)
                            .build()
                            .merge(config);
                }
                return config;
            }
        });
    }

    @Bean
    public RestClient invoiceServiceClient(GeneratorProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();

        return RestClient.builder()
                .baseUrl(props.getTarget())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }

}
