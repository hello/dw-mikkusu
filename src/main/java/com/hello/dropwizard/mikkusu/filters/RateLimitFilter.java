package com.hello.dropwizard.mikkusu.filters;

import com.hello.dropwizard.mikkusu.RateLimiter;
import com.hello.dropwizard.mikkusu.RedisMemoryRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {

    private final RedisMemoryRateLimiter rateLimiter;
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

    public RateLimitFilter(RedisMemoryRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String ip = httpReq.getRemoteAddr();
        LOGGER.debug(String.format("Key = %s", ip));
        long remaining = rateLimiter.rateLimit(ip);
        LOGGER.debug(String.format("Remaining for Key = %s is %d", ip, remaining));
        httpResp.addHeader("rate-limit-remaining", String.valueOf(remaining));

        if (remaining == 0) {
            httpResp.sendError(429, "rate limit");
            return;
        }
        chain.doFilter(httpReq, httpResp);
    }

    @Override
    public void destroy() {
        rateLimiter.shutDown();
        System.out.println("Destroying RateLimitFilter");
    }
}
