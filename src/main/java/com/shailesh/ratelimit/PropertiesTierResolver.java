package com.shailesh.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link TierResolver} that maps identities from the configured
 * {@code starter.rate-limiter.tiers.*} map, falling back to the global default tier.
 */
public class PropertiesTierResolver implements TierResolver {

    private final Map<String, RateLimitTier> tiers;
    private final RateLimitTier defaultTier;

    public PropertiesTierResolver(Map<String, RateLimitTier> tiers, RateLimitTier defaultTier) {
        this.tiers = tiers;
        this.defaultTier = defaultTier;
    }

    public static PropertiesTierResolver from(RateLimitProperties properties) {
        Map<String, RateLimitTier> tiers = new HashMap<>();
        for (Map.Entry<String, RateLimitProperties.Tier> entry : properties.getTiers().entrySet()) {
            RateLimitProperties.Tier tier = entry.getValue();
            tiers.put(entry.getKey(),
                    new RateLimitTier(entry.getKey(), tier.getRequestsPerMinute(), tier.getBurstCapacity()));
        }
        RateLimitProperties.Defaults defaults = properties.getDefaults();
        RateLimitTier defaultTier =
                new RateLimitTier("default", defaults.getRequestsPerMinute(), defaults.getBurstCapacity());
        return new PropertiesTierResolver(tiers, defaultTier);
    }

    @Override
    public RateLimitTier resolve(HttpServletRequest request, String identity) {
        return tiers.getOrDefault(identity, defaultTier);
    }
}