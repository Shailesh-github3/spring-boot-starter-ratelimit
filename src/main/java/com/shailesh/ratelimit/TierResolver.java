package com.shailesh.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy for resolving the rate limit tier for a given request identity.
 * Implementations may be properties-based, database-backed, or fully custom.
 * A single custom {@link TierResolver} bean supplied by the consumer overrides
 * all built-in resolvers.
 */
@FunctionalInterface
public interface TierResolver {

    /**
     * @param request  the current servlet request (nullable for non-filter callers)
     * @param identity the identity extracted from the configured header (e.g. an API key)
     * @return the tier to enforce, or {@code null} to allow the request without limiting
     */
    RateLimitTier resolve(HttpServletRequest request, String identity);
}