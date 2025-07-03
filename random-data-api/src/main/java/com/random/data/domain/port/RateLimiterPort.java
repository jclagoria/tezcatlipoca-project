package com.random.data.domain.port;

import com.random.data.domain.exception.RateLimitExceededException;

public interface RateLimiterPort {

    /**
     * Checks and consumes a permit for the given key (e.g. request path or client ID).
     * @throws RateLimitExceededException if no permits are available
     */
    void consume(String key);
}
