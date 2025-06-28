package com.random.data.adapter.outbound.ratelimiter;

import com.random.data.domain.port.RateLimiterPort;
import com.random.data.domain.port.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Bucket4jLimiterService implements RateLimiterPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bucket4jLimiterService.class);

    private final Bandwidth globalLimit;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Counter tokensConsumed;
    private final Counter throttleEvents;

    /**
     * @param capacity            max tokens in bucket
     * @param refillPeriodMinutes period (minutes) after which bucket is refilled
     * @param registry            the application MetricRegistry
     */
    @Inject
    public Bucket4jLimiterService(
            @ConfigProperty(name = "ratelimiter.capacity") int capacity,
            @ConfigProperty(name = "ratelimiter.refillPeriodMinutes") int refillPeriodMinutes,
            @RegistryType(type = MetricRegistry.Type.APPLICATION) MetricRegistry registry
    ) {
        // Smooth refill: spread `capacity` tokens evenly over the period
        Duration interval = Duration.ofMinutes(refillPeriodMinutes).dividedBy(capacity);
        this.globalLimit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(1, interval)
                .build();

        this.tokensConsumed = registry.counter("bucket4j_tokens_consumed");
        this.throttleEvents = registry.counter("bucket4j_throttle_events");

        LOGGER.info(
                "Bucket4jLimiterService initialized: capacity={} tokens, refill interval={}s",
                capacity, interval.getSeconds()
        );
    }

    /**
     * Attempts to consume one token for the given key.
     * @param key a client‐specific identifier (e.g., IP or API key)
     * @throws RateLimitExceededException if no tokens remain
     */
    @Override
    public void consume(String key) {
        // Lazily create a bucket for this key if absent
        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            LOGGER.debug("Creating new bucket for key={}", k);
            return Bucket.builder()
                    .addLimit(globalLimit)
                    .build();
        });

        if (bucket.tryConsume(1)) {
            tokensConsumed.inc();
            LOGGER.debug("Token consumed for key={}, {} tokens remaining", key, bucket.getAvailableTokens());
        } else {
            throttleEvents.inc();
            Instant now = Instant.now();
            LOGGER.warn("Rate limit exceeded for key={} at {}", key, now);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded for key=%s at %s", key, now)
            );
        }
    }
}
