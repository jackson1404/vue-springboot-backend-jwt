package com.jackson.vue.jwt_backend_integrate.dto.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEventDto(
        String userId,
        String action,
        String resource,
        String ipAddress,
        Instant timeStamp,
        Map<String, Object> metadata
) {}
