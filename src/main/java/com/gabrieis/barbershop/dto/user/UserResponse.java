package com.gabrieis.barbershop.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID publicId,
        String name,
        String email,
        String phone,
        String role
) {
}
