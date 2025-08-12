/***************************************************************
 * Author       :	 
 * Created Date :	
 * Version      : 	
 * History  :	
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.aop;

import com.jackson.vue.jwt_backend_integrate.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

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

    



}
