# spring-boot-starter-ratelimit

Distributed API rate limiting starter for Spring Boot, backed by [Bucket4j](https://github.com/bucket4j/bucket4j) and Redis.

- Per-identity token buckets stored in **Redis** (shared across nodes via Bucket4j's `JedisBasedProxyManager`)
- Auto-registered servlet filter (`/*`) with standard rate-limit response headers
- Configurable tiers: properties-driven by default, or database-driven via JDBC
- Pluggable `TierResolver` strategy — bring your own bean to fully customize

## Install (JitPack)

Add the JitPack repository and the dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Shailesh-github3</groupId>
    <artifactId>spring-boot-starter-ratelimit</artifactId>
    <version>v1.0.2</version>
</dependency>
```

Trigger the first build once by opening `https://jitpack.io/#Shailesh-github3/spring-boot-starter-ratelimit/v1.0.2`.

## Getting started

1. Add the starter and make sure you have a Redis instance running.
2. Nothing else is required — the filter activates automatically.

```yaml
starter:
  rate-limiter:
    header-name: X-API-Key             # identity header read by the filter
    defaults:
      requests-per-minute: 60
      burst-capacity: 60
```

Call an endpoint with the identity header. Every identity consumes from its own bucket;
exceeding the limit returns `429 Too Many Requests` plus `Retry-After`.

## Configuration reference

All properties are under the `starter.rate-limiter` prefix.

| Property | Default | Description |
|---|---|---|
| `enabled` | `true` | Whether the filter is registered |
| `header-name` | `X-API-Key` | Header used to derive the identity (bucket key) |
| `bucket-key-prefix` | `bucket:` | Redis key prefix for buckets |
| `bucket-expiration` | `10m` | Max time a bucket entry is kept in Redis after refill |
| `database-enabled` | `false` | Use the JDBC-backed tier resolver instead of properties |
| `filter-order` | `0` | Order of the registered filter |
| `defaults.requests-per-minute` | `60` | Fallback limit when an identity has no tier |
| `defaults.burst-capacity` | `60` | Fallback burst size |
| `tiers.<identity>.requests-per-minute` | | Per-identity limit (properties mode) |
| `tiers.<identity>.burst-capacity` | | Per-identity burst |
| `redis.host` | `localhost` | Redis host |
| `redis.port` | `6379` | Redis port |
| `redis.password` | | Redis password (blank = none) |
| `redis.max-total` | `50` | Jedis pool max connections |
| `redis.max-idle` | `20` | Jedis pool max idle |
| `redis.min-idle` | `5` | Jedis pool min idle |
| `redis.test-on-borrow` | `true` | Validate connections on borrow |

### Properties mode (default)

```yaml
starter:
  rate-limiter:
    defaults:
      requests-per-minute: 10
      burst-capacity: 10
    tiers:
      user-123:
        requests-per-minute: 1000
        burst-capacity: 2000
      user-456:
        requests-per-minute: 100
        burst-capacity: 200
```

Identities without an entry fall back to `defaults`.

### Database mode

Requires a `rate_limit_tiers` table and a JDBC `DataSource` (e.g. add
`spring-boot-starter-jdbc` or `spring-boot-starter-data-jpa`). The tier is
read with `JdbcTemplate`, so the starter never interferes with the application's
own JPA entity scanning. Enable it:

```yaml
starter:
  rate-limiter:
    database-enabled: true
    defaults:
      requests-per-minute: 10
      burst-capacity: 10
```

```sql
CREATE TABLE rate_limit_tiers (
    identity              VARCHAR(64) PRIMARY KEY,
    requests_per_minute   INT NOT NULL,
    burst_capacity        INT NOT NULL
);

INSERT INTO rate_limit_tiers (identity, requests_per_minute, burst_capacity) VALUES
    ('user-123', 1000, 2000),
    ('user-456', 100, 200);
```

If the identity has no row, `defaults` applies.

### Custom tier resolution

Define your own `TierResolver` bean to control tier assignment from any source
(e.g. roles, tenant, plan):

```java
@Bean
TierResolver tierResolver() {
    return (request, identity) -> new RateLimitTier("premium", 5000, 10000);
}
```

A custom bean disables both built-in resolvers.

## Response headers

| Header | Meaning |
|---|---|
| `X-RateLimit-Limit` | Tier limit (requests per minute) |
| `X-RateLimit-Remaining` | Tokens remaining after this request |
| `Retry-After` | Seconds to wait (only on `429`) |

Requests without the identity header are forwarded untouched.

## Filter ordering vs Spring Security

The filter is registered with `FilterRegistrationBean` at order
`starter.rate-limiter.filter-order` (default `0`). Spring Security's filter is
registered at order `-100`, so by default the rate limiter runs **after**
authentication/authorization.

- Leave the default if rate limits should only apply to authenticated identities.
- Set `starter.rate-limiter.filter-order: -200` to rate limit **before** the
  security chain (e.g. to protect the login endpoint against brute force).

## Overriding beans

Declarations with `@ConditionalOnMissingBean` — provide your own to override:
- `JedisPool`
- `JedisBasedProxyManager<String>`
- `TierResolver`
- `RateLimitService`

## Building locally

```bash
mvn clean install
```

Requires Java 17+. The artifact is a pure library (no Boot plugin, no fat jar).

## Reference implementation

Extracted from the [api-gateway](https://github.com/Shailesh-github3/api-gateway)
project's `com.gateway.ratelimit` package. See `RESEARCH_RESULTS.md` there for the
original performance/architecture research.