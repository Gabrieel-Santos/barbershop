package com.gabrieis.barbershop.dto.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursResponse(
        UUID publicId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean isActive
) {
}
