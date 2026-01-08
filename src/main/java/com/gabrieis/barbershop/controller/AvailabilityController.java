package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.availability.AvailabilityResponse;
import com.gabrieis.barbershop.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> get(@RequestParam UUID barbershopId, @RequestParam UUID professionalId,
                                                    @RequestParam String date,
                                                    @RequestParam(required = false) UUID serviceId
    ) {
        AvailabilityResponse response = availabilityService.getAvailability(barbershopId, professionalId, LocalDate.parse(date), serviceId);
        return ResponseEntity.ok(response);
    }
}
