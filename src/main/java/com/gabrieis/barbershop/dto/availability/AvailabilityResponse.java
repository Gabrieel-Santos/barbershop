package com.gabrieis.barbershop.dto.availability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID barbershopPublicId,
        UUID professionalPublicId,
        LocalDate date,
        Integer slotMinutes,
        Integer durationMinutes,
        List<LocalDateTime> availableStartTimes
) {
}
