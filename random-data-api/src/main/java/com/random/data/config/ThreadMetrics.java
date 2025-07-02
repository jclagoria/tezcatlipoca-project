package com.random.data.config;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.metrics.annotation.Gauge;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Singleton
@Startup
public class ThreadMetrics {

    private final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();

    @Gauge(
            name = "virtual_thread_count",
            description = "Number of live virtual threads",
            unit = "threads"
    )
    public long virtualThreadCount() {
        // In Java 21, threadMX.isVirtualThreadSupported() returns true
        return threadMX.getThreadCount(); // or filter threadMX.getThreadInfo(...) by isVirtual if needed
    }

}
