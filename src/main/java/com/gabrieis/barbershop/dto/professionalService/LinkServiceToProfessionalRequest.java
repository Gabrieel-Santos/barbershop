package com.gabrieis.barbershop.dto.professionalService;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public record LinkServiceToProfessionalRequest(
        UUID servicePublicId,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price override must be greater than zero")
        BigDecimal priceOverride,

        @Min(value = 1, message = "Duration override must be at least 1 minute")
        @Max(value = 600, message = "Duration override must be at most 600 minutes")
        Integer durationOverride
) {
}
