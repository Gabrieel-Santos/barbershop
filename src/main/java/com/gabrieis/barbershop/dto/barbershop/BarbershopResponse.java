package com.gabrieis.barbershop.dto.barbershop;

import java.util.UUID;

public record BarbershopResponse(
        UUID publicId,
        String name,
        String description,
        String phone,
        String email,
        String logoUrl,
        UUID ownerPublicId
) {
}
