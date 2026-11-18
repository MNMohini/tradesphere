package com.bnagritech.tradesphere.common.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleRateLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    /** Returns true and records the request if it is within the limit, false if the limit is reached. */
    public boolean tryAcquire(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);

        if (hits.size() > CLEANUP_THRESHOLD) {
            hits.values().removeIf(q -> {
                synchronized (q) {
                    return q.isEmpty() || q.peekLast().isBefore(now.minus(Duration.ofHours(1)));
                }
            });
        }

        Deque<Instant> queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(cutoff)) {
                queue.pollFirst();
            }
            if (queue.size() >= maxRequests) {
                return false;
            }
            queue.addLast(now);
            return true;
        }
    }
}