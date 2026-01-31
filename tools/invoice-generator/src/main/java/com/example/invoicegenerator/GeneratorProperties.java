package com.example.invoicegenerator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gen")
@Getter
@Setter
public class GeneratorProperties {
    private String target = "http://localhost:8083";
    private int rps = 50;
    private int durationSeconds = 30;
    private int concurrency = 100;
    private int customers = 200;
    private int avgLines = 3;
    private int timeoutMs = 5000;
}
