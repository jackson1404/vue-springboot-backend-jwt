/***************************************************************
 * Author       :
 * Created Date :
 * Version      :
 * History  :
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Bucket4jRedisConfig {

    private final RedissonClient redissonClient; // Injected via Spring Boot Redis Starter or manually

    public Bucket4jRedisConfig(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Creates a ProxyManager which acts as the entry point to get and manage Redis-backed buckets.
     * This manager handles the interaction with the RedissonClient to store and retrieve bucket state.
     * The builder is now correctly invoked with the CommandAsyncExecutor object.
     */
    @Bean
    public ProxyManager<String> bucket4jProxyManager() {
        CommandAsyncExecutor commandAsyncExecutor = (CommandAsyncExecutor) redissonClient;
        return RedissonBasedProxyManager.<String>builderFor(commandAsyncExecutor)
                .build();
    }


    /**
     * Defines the rate limiting rules. This configuration will be used as a template
     * for every new bucket created for a specific key (e.g., a user ID or IP address).
     *
     * This method has been updated to use the modern, non-deprecated API for creating
     * Bandwidth and Refill configurations.
     *
     * @return The configured BucketConfiguration.
     */
    @Bean
    public BucketConfiguration bucketConfiguration() {
        // Defines a limit of 5 tokens, refilling 5 tokens every 1 minute.
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
