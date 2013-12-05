package com.hello.dropwizard.mikkusu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisMemoryRateLimiter implements RateLimiter<String, Long> {

    final private Jedis redis;
    final private int ttl;
    final private long upperLimit;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMemoryRateLimiter.class);

    // TODO: Replace Jedis connection by a connection Pool

    public RedisMemoryRateLimiter(final Jedis redis, final long upperLimit, final int ttl) {
        this.upperLimit = upperLimit;
        this.ttl = ttl;
        this.redis = redis;
    }

    @Override
    public Long rateLimit(final String key) {

        final String used = redis.get(key);
        if(used != null && Long.valueOf(used) > upperLimit) {
            LOGGER.debug(String.format("%s is being rate-limited right now", key));
            return 0L;
        }

        // WARNING: This is leaking a key
        // if the IP is never seen again

        final long value = redis.incr(key);
        if (value == 1) {
            redis.expire(key, ttl);
        }

        final long remaining = upperLimit - value;
        return Math.max(remaining, 0);
    }

    @Override
    public void shutDown() {
        redis.disconnect();
    }
}
