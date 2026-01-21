package com.example.invoicegenerator;

import com.example.invoicegenerator.config.Config;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficStats {

    private final MeterRegistry registry;

    /**
     * Records the duration and outcome of a specific HTTP request.
     * <p>
     * This method categorizes the response into "buckets" (2xx, 4xx, 5xx) using tags,
     * allowing for multidimensional analysis of latency vs status codes.
     *
     * @param durationMs Execution time of the request in milliseconds
     * @param statusCode The HTTP response code received from the target system
     */
    public void recordRequest(long durationMs, int statusCode) {
        String mappedStatusCode = ResponseClassifier.mapToStatusBucket(statusCode);

        registry.timer(Config.INVOICE_TIMER_NAME, "status", mappedStatusCode)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Increments a counter for non-HTTP errors.
     * <p>
     * <b>Use cases:</b>
     * <ul>
     * <li>Network timeouts or "Connection Refused"</li>
     * <li>Backpressure rejections from the internal Semaphore</li>
     * </ul>
     */
    public void markOtherError() {
        registry.counter("invoice.requests.errors", "type", "network_or_backpressure").increment();
    }

    /**
     * Provides a human-readable snapshot of current performance in the logs.
     * <p>
     * This method serves as a "heartbeat" for developers monitoring the console.
     * It pulls real-time data from the {@link MeterRegistry} to calculate
     * and display the following metrics:
     * <ul>
     * <li><b>Total Sent:</b> The cumulative number of requests processed.</li>
     * <li><b>Avg Latency:</b> The mean execution time of requests.</li>
     * <li><b>In-flight:</b> The current number of concurrent requests being handled.</li>
     * </ul>
     *
     * @param currentInflight the number of requests currently being processed by the system
     */
    public void logCurrentReport(int currentInflight) {
        double total = registry.get("invoice.requests").timer().count();
        double mean = registry.get("invoice.requests").timer().mean(TimeUnit.MILLISECONDS);

        log.info("Report -> Sent: {} | Avg Latency: {}ms | In-flight: {}",
                (long) total, String.format("%.2f", mean), currentInflight);
    }

    public long getSentCount() {
        return (long) registry.find(Config.INVOICE_TIMER_NAME).timers()
                .stream()
                .mapToDouble(Timer::count)
                .sum();
    }

    public long getOkCount() {
        return Objects.requireNonNull(registry.find(Config.INVOICE_TIMER_NAME)
                        .tag("status", "2xx")
                        .timer())
                .count();
    }
}