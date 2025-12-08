package com.gabrieis.barbershop.dto.barbershop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBarbershopRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @NotBlank(message = "Phone is required")
        String phone,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        String logoUrl

) {
}
