package org.minbox.framework.limiter.exception;

import lombok.NoArgsConstructor;

/**
 * RateLimiter exception
 *
 * @author 恒宇少年
 */
@NoArgsConstructor
public class RateLimiterException extends RuntimeException {
    public RateLimiterException(String message) {
        super(message);
    }

    public RateLimiterException(String message, Throwable cause) {
        super(message, cause);
    }
}
