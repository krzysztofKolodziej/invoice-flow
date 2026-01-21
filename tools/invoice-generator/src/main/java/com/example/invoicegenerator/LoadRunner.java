package com.example.invoicegenerator;

import com.example.invoicegenerator.factory.InvoiceFactory;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Main orchestrator for the traffic generation process.
 * <p>
 * This component manages the lifecycle of the load test by:
 * <ul>
 * <li>Scheduling periodic traffic emission (RPS control)</li>
 * <li>Managing concurrency via {@link Semaphore} (Backpressure)</li>
 * <li>Offloading HTTP tasks to Virtual Threads via {@link TaskExecutor}</li>
 * <li>Coordinating periodic status reporting via {@link TrafficStats}</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoadRunner implements ApplicationRunner {

    private final GeneratorProperties props;
    private final InvoiceFactory invoiceFactory;
    private final InvoiceSender invoiceSender;
    private final TrafficStats stats;
    private final TaskExecutor taskExecutor;

    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);

    /**
     * Entry point for the traffic generation logic.
     * <p>
     * Orchestrates the startup of reporting and traffic generation jobs,
     * manages the test duration, and ensures a graceful shutdown.
     *
     * @param args incoming application arguments
     * @throws Exception if any interruption or execution error occurs
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        Semaphore inFlight = new Semaphore(props.getConcurrency());

        log.info("Starting load generation: target={}, rps={}, duration={}s, concurrency={}",
                props.getTarget(), props.getRps(), props.getDurationSeconds(), props.getConcurrency());

        startReporting(inFlight);
        ScheduledFuture<?> trafficJob = startTrafficGeneration(inFlight);

        Thread.sleep(TimeUnit.SECONDS.toMillis(props.getDurationSeconds()));

        stopTraffic(trafficJob);
        waitForInFlightRequests(inFlight);

        log.info("=== Load Generation Completed ===");
        stats.logCurrentReport(0);

        handleFinalExitStatus();
    }

    private ScheduledFuture<?> startTrafficGeneration(Semaphore inFlight) {
        return scheduler.scheduleAtFixedRate(() -> {
            for (int i = 0; i < props.getRps(); i++) {
                if (!inFlight.tryAcquire()) {
                    stats.markOtherError();
                    continue;
                }

                taskExecutor.execute(() -> executeSingleRequest(inFlight));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void executeSingleRequest(Semaphore inFlight) {
        long startTime = System.currentTimeMillis();
        try {
            JsonNode invoice = invoiceFactory.newInvoice();
            int statusCode = invoiceSender.send(invoice);

            long duration = System.currentTimeMillis() - startTime;
            stats.recordRequest(duration, statusCode);

        } catch (Exception e) {
            log.error("Unexpected error in load runner loop: {}", e.getMessage());
            stats.markOtherError();
        } finally {
            inFlight.release();
        }
    }

    private void startReporting(Semaphore inFlight) {
        scheduler.scheduleAtFixedRate(() -> {
            int currentInFlight = props.getConcurrency() - inFlight.availablePermits();
            stats.logCurrentReport(currentInFlight);
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopTraffic(ScheduledFuture<?> trafficJob) {
        log.info("Stopping traffic generation...");
        trafficJob.cancel(false);
        scheduler.shutdown();
    }

    private void waitForInFlightRequests(Semaphore inFlight) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline && inFlight.availablePermits() < props.getConcurrency()) {
            Thread.sleep(100);
        }
    }

    private void handleFinalExitStatus() {
        if (stats.getSentCount() > 0 && stats.getOkCount() == 0) {
            log.error("Termination: Load test failed (0% success rate).");
            System.exit(2);
        } else {
            log.info("Termination: Load test completed successfully.");
            System.exit(0);
        }
    }
}