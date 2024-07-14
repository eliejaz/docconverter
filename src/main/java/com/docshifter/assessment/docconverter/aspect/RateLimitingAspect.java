package com.docshifter.assessment.docconverter.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class RateLimitingAspect {

    private final Bucket bucket;

    public RateLimitingAspect(Bucket bucket) {
        this.bucket = bucket;
    }


    @Around("@annotation(com.docshifter.assessment.docconverter.annotation.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new RuntimeException("Too Many Requests");
        }
    }
}