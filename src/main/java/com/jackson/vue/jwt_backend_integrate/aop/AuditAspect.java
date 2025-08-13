/***************************************************************
 * Author       :	 
 * Created Date :	
 * Version      : 	
 * History  :	
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.aop;

import com.jackson.vue.jwt_backend_integrate.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * AuditAspect Class.
 * <p>
 * </p>
 *
 * @author
 */

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditAnnotation)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Audit auditAnnotation, HttpServletRequest request) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "ANONYMOUS";

        String action = auditAnnotation.action();
        String resource = auditAnnotation.resource();

        // Collect method arguments
        Map<String, Object> metadata = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        String[] params = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < params.length; i++) {
            metadata.put(params[i], args[i]);
        }

        // Before execution → log attempt
        auditService.logEvent(
                username,
                action + "_ATTEMPT",
                resource,
                request.getRemoteAddr(),
                Instant.now(),
                metadata
        );

        try {
            Object result = joinPoint.proceed(); // Execute method

            // After success → log success
            auditService.logEvent(
                    username,
                    action + "_SUCCESS",
                    resource,
                    request.getRemoteAddr(),
                    Instant.now(),
                    metadata
            );

            return result;
        } catch (Throwable ex) {
            // On failure → log failure
            Map<String, Object> errorMeta = new HashMap<>(metadata);
            errorMeta.put("error", ex.getMessage());

            auditService.logEvent(
                    username,
                    action + "_FAILURE",
                    resource,
                    request.getRemoteAddr(),
                    Instant.now(),
                    errorMeta
            );

            throw ex; // Rethrow so normal error handling still applies
        }
    }




}
