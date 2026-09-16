package com.shailesh.ratelimit.db;

import com.shailesh.ratelimit.RateLimitProperties;
import com.shailesh.ratelimit.TierResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Auto-configuration enabling database-driven tiers.
 *
 * <p>Active only when {@code starter.rate-limiter.database-enabled=true} and
 * Spring JDBC (plus a {@link DataSource}) is on the classpath. Reads tiers from
 * the {@code rate_limit_tiers} table through a {@link JdbcTemplate} and replaces
 * the properties-based tier resolver with a {@link DatabaseTierResolver}.
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnClass({JdbcTemplate.class, DataSource.class})
@ConditionalOnProperty(prefix = "starter.rate-limiter", name = "database-enabled", havingValue = "true")
public class DatabaseTierAutoConfiguration {

    private final RateLimitProperties properties;

    public DatabaseTierAutoConfiguration(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(TierResolver.class)
    public TierResolver databaseTierResolver(JdbcTemplate jdbcTemplate) {
        return new DatabaseTierResolver(jdbcTemplate, properties);
    }
}