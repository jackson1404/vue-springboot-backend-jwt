/***************************************************************
 * Author       :
 * Created Date :
 * Version      :
 * History  :
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitingFilter Class.
 * <p>
 * </p>
 *
 * @author
 */
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals("/login") || path.equals("/refreshNewToken")){

            String ip = request.getRemoteAddr();
            Bucket bucket = proxyManager.builder()
                    .build(ip, () -> bucketConfiguration);
            if (bucket.tryConsume(1)){
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429);  //TOO_MANY_REQUESTS status code
                response.getWriter().write("Too many requests - try again later");
                return;
            }

        } else {
            filterChain.doFilter(request, response);
        }

    }

}
