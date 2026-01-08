package com.gabrieis.barbershop.dto.schedule;

import com.gabrieis.barbershop.enums.ScheduleExceptionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleExceptionResponse(
        UUID publicId,
        ScheduleExceptionType type,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String notes
) {
}
