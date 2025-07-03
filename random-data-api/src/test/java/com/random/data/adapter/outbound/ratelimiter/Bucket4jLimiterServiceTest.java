package com.random.data.adapter.outbound.ratelimiter;

import com.random.data.domain.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Bucket4jLimiterServiceTest {

    @Test
    void consume_shouldConsumeToken_andIncrementTokensConsumedCounter() {
        // given
        var fixture = TestFixtures.createLimiterService(1, 1);
        String key = "client1";

        // when
        fixture.service.consume(key);

        // then
        verify(fixture.tokensConsumed).inc();
        // no exception thrown
    }

    @Test
    void consume_shouldThrow_whenCapacityExceeded_andIncrementThrottleCounter() {
        // given
        var fixture = TestFixtures.createLimiterService(1, 1);
        String key = "client1";
        fixture.service.consume(key);

        // when / then
        assertThatThrownBy(() -> fixture.service.consume(key))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded for key=" + key);

        verify(fixture.throttleEvents).inc();
    }

    @ParameterizedTest
    @CsvSource({
            // capacity, calls, shouldThrottleAtLastCall?
            "3, 3, false",
            "3, 4, true"
    })
    void consume_parameterizedCapacity(int capacity, int calls, boolean shouldThrottle) {
        var fixture = TestFixtures.createLimiterService(capacity, 1);
        String key = "paramKey";

        for (int i = 1; i <= calls; i++) {
            if (shouldThrottle && i == calls) {
                // last invocation should throttle
                assertThrows(RateLimitExceededException.class,
                        () -> fixture.service.consume(key));
                verify(fixture.throttleEvents).inc();
            } else {
                fixture.service.consume(key);
                verify(fixture.tokensConsumed, times(i)).inc();
            }
        }
    }

    @Test
    void consume_shouldBeIsolatedBetweenDifferentKeys() {
        // given
        var fixture = TestFixtures.createLimiterService(1, 1);
        String key1 = "key1";
        String key2 = "key2";

        // when: exhaust key1
        fixture.service.consume(key1);
        assertThrows(RateLimitExceededException.class, () -> fixture.service.consume(key1));

        // then: key2 is unaffected
        fixture.service.consume(key2);

        // verify tokensConsumed called once for key1 + once for key2
        verify(fixture.tokensConsumed, times(2)).inc();
    }

}