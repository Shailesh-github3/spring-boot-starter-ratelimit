package com.shailesh.ratelimit;

/**
 * Core domain type describing a rate limit bucket configuration.
 */
public final class RateLimitTier {

    private final String name;
    private final int requestsPerMinute;
    private final int burstCapacity;

    public RateLimitTier(String name, int requestsPerMinute, int burstCapacity) {
        this.name = name;
        this.requestsPerMinute = requestsPerMinute;
        this.burstCapacity = burstCapacity;
    }

    /**
     * Tier name (used as a diagnostic label).
     */
    public String getName() {
        return name;
    }

    /**
     * Steady-state tokens per minute (limit).
     */
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    /**
     * Maximum bucket size (burst tolerance).
     */
    public int getBurstCapacity() {
        return burstCapacity;
    }
}