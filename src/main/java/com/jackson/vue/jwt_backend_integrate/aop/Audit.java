package com.jackson.vue.jwt_backend_integrate.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {
    String action();
    String resource();
}
