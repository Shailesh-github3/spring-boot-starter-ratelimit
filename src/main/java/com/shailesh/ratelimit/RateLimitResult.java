package com.shailesh.ratelimit;

/**
 * Outcome of a single token consumption attempt.
 */
public final class RateLimitResult {

    private final boolean allowed;
    private final long limit;
    private final long remaining;
    private final long retryAfterSeconds;

    public RateLimitResult(boolean allowed, long limit, long remaining, long retryAfterSeconds) {
        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * True if the request may proceed (token consumed).
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Tier limit in requests per minute.
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Tokens left in the bucket after this attempt.
     */
    public long getRemaining() {
        return remaining;
    }

    /**
     * Seconds to wait before retrying when not allowed.
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}