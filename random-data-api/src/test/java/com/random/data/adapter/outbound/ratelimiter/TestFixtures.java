package com.random.data.adapter.outbound.ratelimiter;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;

import static org.mockito.Mockito.*;

public class TestFixtures {

    /**
     * Holds a pre‐configured service and the two counters so tests can verify interactions.
     */
    public static class LimiterServiceFixture {
        public final Bucket4jLimiterService service;
        public final Counter tokensConsumed;
        public final Counter throttleEvents;

        private LimiterServiceFixture(
                Bucket4jLimiterService service,
                Counter tokensConsumed,
                Counter throttleEvents
        ) {
            this.service = service;
            this.tokensConsumed = tokensConsumed;
            this.throttleEvents = throttleEvents;
        }
    }

    /**
     * Creates a Bucket4jLimiterService with mocked MetricRegistry and Counters.
     * @param capacity             bucket capacity
     * @param refillPeriodMinutes  refill period in minutes
     */
    public static LimiterServiceFixture createLimiterService(int capacity, int refillPeriodMinutes) {
        Counter tokensConsumed = mock(Counter.class);
        Counter throttleEvents = mock(Counter.class);
        MetricRegistry registry = mock(MetricRegistry.class);
        when(registry.counter("bucket4j_tokens_consumed")).thenReturn(tokensConsumed);
        when(registry.counter("bucket4j_throttle_events")).thenReturn(throttleEvents);

        Bucket4jLimiterService service =
                new Bucket4jLimiterService(capacity, refillPeriodMinutes, registry);

        return new LimiterServiceFixture(service, tokensConsumed, throttleEvents);
    }

}
