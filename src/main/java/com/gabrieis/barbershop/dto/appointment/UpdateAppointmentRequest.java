package com.gabrieis.barbershop.dto.appointment;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateAppointmentRequest(
        @NotNull
        UUID professionalPublicId,

        @NotNull
        UUID servicePublicId,

        @NotNull
        LocalDateTime startTime,

        String notes
) {
}
