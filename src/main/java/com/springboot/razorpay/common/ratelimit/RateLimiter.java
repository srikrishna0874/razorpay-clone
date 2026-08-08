package com.springboot.razorpay.common.ratelimit;

public interface RateLimiter {

    RateLimitResult checkRateLimit(String key, int maxRequestsAllowed, long windowSeconds);

}
