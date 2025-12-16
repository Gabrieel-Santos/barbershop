package com.gabrieis.barbershop.dto.professional;

import java.util.UUID;

public record ProfessionalResponse(
        UUID publicId,
        String displayName,
        String bio,
        String avatarUrl,
        boolean isActive,
        UUID barbershopPublicId,
        UUID userPublicId
) {
}
