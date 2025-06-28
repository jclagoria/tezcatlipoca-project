package com.random.data.config;

import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MutinyConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MutinyConfiguration.class);

    // Virtual-thread executor for blocking work
    private final ExecutorService vtxExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    public void initialize() {
        LOGGER.info("MutinyConfiguration initializing: setting default executor to virtual-thread pool");
        Infrastructure.setDefaultExecutor(vtxExecutor);
        LOGGER.debug("Default Mutiny executor is now {}", vtxExecutor);
    }

    @PreDestroy
    public void shutdown() {
        LOGGER.info("MutinyConfiguration shutting down: invoking shutdownNow() on virtual-thread executor");
        List<Runnable> pending = vtxExecutor.shutdownNow();
        LOGGER.debug("shutdownNow() returned {} pending task(s)", pending.size());

        try {
            if (vtxExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.info("Virtual-thread executor terminated cleanly");
            } else {
                LOGGER.warn("Virtual-thread executor did not terminate within 30 seconds; forcing exit");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while awaiting virtual-thread executor termination", ie);
        }
    }

}
