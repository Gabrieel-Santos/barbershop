package com.gabrieis.barbershop.dto.auth;

public record AuthResponse(
        String token,
        String refreshToken
) {
}
