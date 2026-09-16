package com.shailesh.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * External configuration for the rate limiter, bound under {@code starter.rate-limiter.*}.
 */
@ConfigurationProperties(prefix = "starter.rate-limiter")
public class RateLimitProperties {

    private boolean enabled = true;
    private String headerName = "X-API-Key";
    private String bucketKeyPrefix = "bucket:";
    private Duration bucketExpiration = Duration.ofMinutes(10);
    private boolean databaseEnabled = false;
    private int filterOrder = 0;
    private Defaults defaults = new Defaults();
    private Redis redis = new Redis();
    private Map<String, Tier> tiers = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getBucketKeyPrefix() {
        return bucketKeyPrefix;
    }

    public void setBucketKeyPrefix(String bucketKeyPrefix) {
        this.bucketKeyPrefix = bucketKeyPrefix;
    }

    public Duration getBucketExpiration() {
        return bucketExpiration;
    }

    public void setBucketExpiration(Duration bucketExpiration) {
        this.bucketExpiration = bucketExpiration;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public void setDatabaseEnabled(boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public Map<String, Tier> getTiers() {
        return tiers;
    }

    public void setTiers(Map<String, Tier> tiers) {
        this.tiers = tiers;
    }

    /**
     * Fallback tier applied when a specific identity has no configured mapping.
     */
    public static class Defaults {
        private int requestsPerMinute = 60;
        private int burstCapacity = 60;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }

    /**
     * Redis connection pool settings for the shared Jedis pool.
     */
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int maxTotal = 50;
        private int maxIdle = 20;
        private int minIdle = 5;
        private boolean testOnBorrow = true;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }
    }

    /**
     * Identity -> tier mapping for properties (non-database) mode.
     */
    public static class Tier {
        private int requestsPerMinute = 60;
        private int burstCapacity = 60;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }
}