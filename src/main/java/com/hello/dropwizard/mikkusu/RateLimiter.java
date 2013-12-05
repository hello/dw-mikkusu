package com.hello.dropwizard.mikkusu;

public interface RateLimiter<K, V> {

    V rateLimit(K key);
    void shutDown();
}
