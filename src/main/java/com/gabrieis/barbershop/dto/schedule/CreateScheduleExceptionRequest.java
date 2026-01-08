package com.gabrieis.barbershop.dto.schedule;

import com.gabrieis.barbershop.enums.ScheduleExceptionType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateScheduleExceptionRequest(
        @NotNull ScheduleExceptionType type,
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        String notes
) {
}
