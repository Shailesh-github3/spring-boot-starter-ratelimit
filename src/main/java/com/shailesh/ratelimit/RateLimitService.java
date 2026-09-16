package com.shailesh.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Duration;

/**
 * Distributed token-bucket service. Each identity gets its own Redis-backed bucket.
 * The bucket configuration is derived from the tier resolved for the request.
 */
public class RateLimitService {

    private final JedisBasedProxyManager<String> proxyManager;
    private final TierResolver tierResolver;
    private final String bucketKeyPrefix;
    private final Duration bucketExpiration;

    public RateLimitService(JedisBasedProxyManager<String> proxyManager,
                            TierResolver tierResolver,
                            String bucketKeyPrefix,
                            Duration bucketExpiration) {
        this.proxyManager = proxyManager;
        this.tierResolver = tierResolver;
        this.bucketKeyPrefix = bucketKeyPrefix;
        this.bucketExpiration = bucketExpiration;
    }

    public RateLimitResult tryConsume(HttpServletRequest request, String identity) {
        RateLimitTier tier = tierResolver.resolve(request, identity);
        if (tier == null) {
            return new RateLimitResult(true, 0, 0, 0);
        }

        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(tier.getBurstCapacity())
                        .refillGreedy(tier.getRequestsPerMinute(), Duration.ofMinutes(1)))
                .build();

        Bucket bucket = proxyManager.getProxy(bucketKeyPrefix + identity, () -> config);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
        return new RateLimitResult(
                probe.isConsumed(),
                tier.getRequestsPerMinute(),
                probe.getRemainingTokens(),
                retryAfterSeconds);
    }
}