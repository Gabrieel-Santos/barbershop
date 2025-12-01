package com.gabrieis.barbershop.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
