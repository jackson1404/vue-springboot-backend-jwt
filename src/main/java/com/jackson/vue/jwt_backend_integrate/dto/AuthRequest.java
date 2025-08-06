package com.jackson.vue.jwt_backend_integrate.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}