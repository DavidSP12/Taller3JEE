package com.taller3jee.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
