package com.jackson.vue.jwt_backend_integrate.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

//    private final RedisTemplate<String, String> redisTemplate;
//
//    private static final String BUCKET_KEY_PREFIX = "rl:";

    private static final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals("/login") || path.equals("/refreshNewToken")) {

            String ip = request.getRemoteAddr();
//            String key = BUCKET_KEY_PREFIX + ip;

            Bucket bucket = cache.computeIfAbsent(ip, k -> newBucket(ip));

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests - try again later");
            }

        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

//    private BucketConfiguration getDefaultConfig() {
//        Bandwidth limit = Bandwidth.builder()
//                .capacity(5)
//                .refillGreedy(5, Duration.ofMinutes(1))
//                .build();
//        return BucketConfiguration.builder()
//                .addLimit(limit)
//                .build();
//    }
//
//    private Bucket loadBucketFromRedis(String key) {
//        String data = redisTemplate.opsForValue().get(key);
//        if (data != null) {
//            byte[] stateBytes = Base64.getDecoder().decode(data);
//            return Bucket4j.extension(io.github.bucket4j.serialization.JacksonSerializer.get())
//                    .deserializeBucket(stateBytes);
//        } else {
//            return Bucket4j.builder()
//                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
//                    .build();
//        }
//    }
//
//    private void saveBucketToRedis(String key, Bucket bucket) {
//        byte[] stateBytes = bucket.serializeToBytes();
//        String encoded = Base64.getEncoder().encodeToString(stateBytes);
//        redisTemplate.opsForValue().set(key, encoded, Duration.ofMinutes(1)); // TTL same as refill period
//    }
}
