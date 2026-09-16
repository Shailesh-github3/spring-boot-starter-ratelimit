package com.shailesh.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.serialization.Mapper;
import io.github.bucket4j.redis.jedis.Bucket4jJedis;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Auto-configuration for Redis-backed distributed rate limiting.
 *
 * <p>Registers a shared {@link JedisPool}, a Bucket4j {@link JedisBasedProxyManager},
 * the default properties-based {@link TierResolver}, the {@link RateLimitService}
 * and a {@link RateLimitFilter} against {@code /*}.
 *
 * <p>Consumers may override any bean (pool, proxy manager, tier resolver, service)
 * by declaring their own bean of the same type.
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({JedisPool.class, JedisBasedProxyManager.class})
public class RateLimitAutoConfiguration {

    private final RateLimitProperties properties;

    public RateLimitAutoConfiguration(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(JedisPool.class)
    public JedisPool jedisPool() {
        RateLimitProperties.Redis redis = properties.getRedis();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redis.getMaxTotal());
        poolConfig.setMaxIdle(redis.getMaxIdle());
        poolConfig.setMinIdle(redis.getMinIdle());
        poolConfig.setTestOnBorrow(redis.isTestOnBorrow());

        if (redis.getPassword() != null && !redis.getPassword().isBlank()) {
            return new JedisPool(poolConfig, redis.getHost(), redis.getPort(), 2000, redis.getPassword());
        }
        return new JedisPool(poolConfig, redis.getHost(), redis.getPort());
    }

    @Bean
    @ConditionalOnMissingBean(JedisBasedProxyManager.class)
    public JedisBasedProxyManager<String> bucketProxyManager(JedisPool jedisPool) {
        return Bucket4jJedis.casBasedBuilder(jedisPool)
                .expirationAfterWrite(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(properties.getBucketExpiration()))
                .keyMapper(Mapper.STRING)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(TierResolver.class)
    @ConditionalOnProperty(prefix = "starter.rate-limiter", name = "database-enabled",
            havingValue = "false", matchIfMissing = true)
    public TierResolver tierResolver() {
        return PropertiesTierResolver.from(properties);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitService.class)
    public RateLimitService rateLimitService(JedisBasedProxyManager<String> proxyManager,
                                             TierResolver tierResolver) {
        return new RateLimitService(proxyManager, tierResolver,
                properties.getBucketKeyPrefix(), properties.getBucketExpiration());
    }

    @Bean
    @ConditionalOnProperty(prefix = "starter.rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitService rateLimitService) {
        RateLimitFilter filter = new RateLimitFilter(rateLimitService, properties.getHeaderName());
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}