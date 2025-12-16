package com.gabrieis.barbershop.dto.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProfessionalRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 150, message = "Display name must be at most 150 characters")
        String displayName,

        @Size(max = 500, message = "Bio must be at most 500 characters")
        String bio,

        String avatarUrl,

        String userPublicId

) {
}
