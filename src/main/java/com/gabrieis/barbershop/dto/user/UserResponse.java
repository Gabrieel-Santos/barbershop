package com.gabrieis.barbershop.dto.user;

import com.gabrieis.barbershop.enums.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID publicId,
        String name,
        String email,
        String phone,
        UserRole role,
        boolean isActive
) {
}
