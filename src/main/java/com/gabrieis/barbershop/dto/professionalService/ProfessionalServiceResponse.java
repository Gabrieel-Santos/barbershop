package com.gabrieis.barbershop.dto.professionalService;

import java.math.BigDecimal;
import java.util.UUID;

public record ProfessionalServiceResponse(
        UUID publicId,
        UUID professionalPublicId,
        UUID servicePublicId,
        String serviceName,
        BigDecimal price,
        Integer durationMinutes,
        boolean isActive
) {
}
