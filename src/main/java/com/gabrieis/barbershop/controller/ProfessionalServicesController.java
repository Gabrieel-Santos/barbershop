package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.professionalService.LinkServiceToProfessionalRequest;
import com.gabrieis.barbershop.dto.professionalService.ProfessionalServiceResponse;
import com.gabrieis.barbershop.dto.professionalService.UpdateProfessionalServiceRequest;
import com.gabrieis.barbershop.service.ProfessionalServicesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/professionals")
public class ProfessionalServicesController {

    private final ProfessionalServicesService professionalServicesService;

    @PostMapping("/{professionalPublicId}/services")
    public ResponseEntity<ProfessionalServiceResponse> linkService(
            @PathVariable UUID professionalPublicId, @Valid @RequestBody LinkServiceToProfessionalRequest request) {
        ProfessionalServiceResponse response = professionalServicesService.linkServiceToProfessional(professionalPublicId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{professionalPublicId}/services")
    public ResponseEntity<List<ProfessionalServiceResponse>> listLinks(@PathVariable UUID professionalPublicId) {
        List<ProfessionalServiceResponse> response =
                professionalServicesService.listProfessionalServices(professionalPublicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barbershops/{barbershopPublicId}/professionals/{professionalPublicId/services}")
    public ResponseEntity<List<ProfessionalServiceResponse>> listActiveServicesForProfessional(
            @PathVariable UUID barbershopPublicId,
            @PathVariable UUID professionalPublicId
    ) {
        List<ProfessionalServiceResponse> response = professionalServicesService.listActiveServicesForProfessional(barbershopPublicId, professionalPublicId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/services/{professionalServicePublicId}")
    public ResponseEntity<ProfessionalServiceResponse> updatedLink(@PathVariable UUID professionalServicePublicId,
                                                                   @Valid @RequestBody UpdateProfessionalServiceRequest request) {
        ProfessionalServiceResponse response = professionalServicesService.updateProfessionalService(professionalServicePublicId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{professionalPublicId}/services/{servicePublicId}")
    public ResponseEntity<Void> unlink(@PathVariable UUID professionalPublicId, UUID servicePublicId) {
        professionalServicesService.unlinkService(professionalPublicId, servicePublicId);
        return ResponseEntity.noContent().build();
    }

}
