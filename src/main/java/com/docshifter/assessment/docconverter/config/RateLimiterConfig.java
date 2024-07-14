package com.docshifter.assessment.docconverter.config;


import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Value("${rate.limiter.capacity}")
    private int capacity;
    @Value("${rate.limiter.tokens}")
    private int tokens;
    @Value("${rate.limiter.duration}")
    private int durationInMinutes;

    @Bean
    public Bucket rateLimiterBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(tokens, Duration.ofMinutes(durationInMinutes)))
                .build();
    }
}
