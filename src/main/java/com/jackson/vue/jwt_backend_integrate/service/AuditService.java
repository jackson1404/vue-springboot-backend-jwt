/***************************************************************
 * Author       :	 
 * Created Date :	
 * Version      : 	
 * History  :	
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.service;

import com.jackson.vue.jwt_backend_integrate.dto.audit.AuditEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * AuditService Class.
 * <p>
 * </p>
 *
 * @author
 */

@Service
@Slf4j
public class AuditService {

    public void logEvent(String userId,
                         String action,
                         String resource,
                         String ipAddress,
                         Instant timeStamp,
                         Map<String, Object> metadata){

        AuditEventDto eventDto = new AuditEventDto(
                userId, action, resource, ipAddress, timeStamp, metadata);

        log.info("AUDIT_LOG: {}", eventDto);

    }

}
