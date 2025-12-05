package com.gabrieis.barbershop.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Google idToken is required")
        String idToken) {

}
