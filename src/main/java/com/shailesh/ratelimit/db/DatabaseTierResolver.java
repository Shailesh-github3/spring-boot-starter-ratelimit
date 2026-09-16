package com.shailesh.ratelimit.db;

import com.shailesh.ratelimit.RateLimitProperties;
import com.shailesh.ratelimit.RateLimitTier;
import com.shailesh.ratelimit.TierResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * {@link TierResolver} backed by the {@code rate_limit_tiers} table, queried
 * through a {@link JdbcTemplate}. Uses plain JDBC instead of a managed JPA entity
 * so the starter's table never interferes with the application's own entity scanning.
 * Falls back to the configured default tier when an identity has no row.
 */
public class DatabaseTierResolver implements TierResolver {

    private static final String SELECT_TIER =
            "SELECT requests_per_minute, burst_capacity FROM rate_limit_tiers WHERE identity = ?";

    private final JdbcTemplate jdbcTemplate;
    private final RateLimitTier defaultTier;

    public DatabaseTierResolver(JdbcTemplate jdbcTemplate, RateLimitProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        RateLimitProperties.Defaults defaults = properties.getDefaults();
        this.defaultTier = new RateLimitTier("default", defaults.getRequestsPerMinute(), defaults.getBurstCapacity());
    }

    @Override
    public RateLimitTier resolve(HttpServletRequest request, String identity) {
        List<RateLimitTier> matches = jdbcTemplate.query(SELECT_TIER,
                (rs, rowNum) -> new RateLimitTier(identity,
                        rs.getInt("requests_per_minute"),
                        rs.getInt("burst_capacity")),
                identity);
        return matches.isEmpty() ? defaultTier : matches.get(0);
    }
}