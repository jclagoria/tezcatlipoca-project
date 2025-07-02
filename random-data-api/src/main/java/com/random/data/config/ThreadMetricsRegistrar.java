package com.random.data.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

@ApplicationScoped
public class ThreadMetricsRegistrar {

    @Inject
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    MetricRegistry registry;

    void onStart(@Observes StartupEvent ev) {
        ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
        // enable CPU & contention metrics
        if (threadMX.isThreadCpuTimeSupported()) threadMX.setThreadCpuTimeEnabled(true);
        if (threadMX.isThreadContentionMonitoringSupported())
            threadMX.setThreadContentionMonitoringEnabled(true);

        // 1) Total live threads
        registry.register("thread_count", (Gauge<Integer>) threadMX::getThreadCount);

        // 2) Daemon threads
        registry.register("daemon_thread_count", (Gauge<Integer>) threadMX::getDaemonThreadCount);

        // 3) Peak live threads
        registry.register("peak_thread_count", (Gauge<Integer>) threadMX::getPeakThreadCount);

        // 4) Current total CPU time (sum of all live threads)
        registry.register("total_thread_cpu_time", (Gauge<Long>) () ->
                Arrays.stream(threadMX.getAllThreadIds())
                        .map(threadMX::getThreadCpuTime)
                        .filter(t -> t != -1)
                        .sum()
        );

        // 5) Number of deadlocked threads
        registry.register("deadlocked_thread_count", (Gauge<Integer>) () -> {
            long[] dl = threadMX.findDeadlockedThreads();
            return dl == null ? 0 : dl.length;
        });

        // (Optional) reset the peak counter every scrape interval
        registry.register("reset_peak_thread_count", (Gauge<Boolean>) () -> {
            threadMX.resetPeakThreadCount();
            return Boolean.TRUE;
        });
    }

}
