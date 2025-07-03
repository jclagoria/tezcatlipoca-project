package com.random.data.adapter.outbound.provider;

import com.random.data.domain.exception.DataUnavailableException;
import com.random.data.domain.port.DataProvider;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.*;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.Collections;
import java.util.List;

public abstract class AbstractDataProvider<T> implements DataProvider<T> {

    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.5,
            delay = 5000L
    )
    @Retry(
            maxRetries = 3,
            delay = 1000L,
            jitter = 100L,
            retryOn      = { DataUnavailableException.class },
            abortOn      = { IllegalArgumentException.class }
    )
    @Timeout(5000L)
    @Bulkhead(
            value = 10,
            waitingTaskQueue = 100
    )
    @Fallback(fallbackMethod = "fallbackGenerate")
    @Counted(name = "dataGenerate_invocations_total", description = "Total generate() calls")
    @Timed(name = "dataGenerate_duration_seconds", description = "Time taken by generate()")
    public Uni<List<T>> generate(String locale, int count) {
        return doGenerate(locale, count);
    }

    /**
     * Actual provider logic to be implemented by subclasses
     */
    protected abstract Uni<List<T>> doGenerate(String locale, int count);

    /**
     * Fallback method: return empty list on failure
     */
    protected Uni<List<T>> fallbackGenerate(String locale, int count, Throwable t) {
        return Uni.createFrom().item(Collections.emptyList());
    }
}
