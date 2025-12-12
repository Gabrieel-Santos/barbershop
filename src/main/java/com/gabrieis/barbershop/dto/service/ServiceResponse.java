package com.gabrieis.barbershop.dto.service;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        UUID publicId,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        UUID barbershopPublicId
) {
}
