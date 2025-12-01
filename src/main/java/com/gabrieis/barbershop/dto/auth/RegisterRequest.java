package com.gabrieis.barbershop.dto.auth;

public record RegisterRequest(
        String name,
        String email,
        String phone,
        String password
) {
}
