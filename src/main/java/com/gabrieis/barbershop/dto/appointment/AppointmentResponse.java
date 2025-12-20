package com.gabrieis.barbershop.dto.appointment;

import com.gabrieis.barbershop.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(

        UUID publicId,
        UUID barbershopPublicId,
        UUID clientPublicId,
        UUID professionalPublicId,
        String professionalName,
        UUID servicePublicId,
        String serviceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        String notes
) {
}
