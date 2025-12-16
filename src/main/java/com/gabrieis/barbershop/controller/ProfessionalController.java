package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.professional.CreateProfessionalRequest;
import com.gabrieis.barbershop.dto.professional.ProfessionalResponse;
import com.gabrieis.barbershop.dto.professional.UpdateProfessionalRequest;
import com.gabrieis.barbershop.service.ProfessionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody CreateProfessionalRequest request) {
        ProfessionalResponse response = professionalService.createProfessional(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProfessionalResponse>> listMyProfessionals() {
        List<ProfessionalResponse> professionals = professionalService.listMyProfessionals();
        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/barbershop/{barbershopPublicId}")
    public ResponseEntity<List<ProfessionalResponse>> listByBarbershop(@PathVariable UUID barbershopPublicId) {
        List<ProfessionalResponse> professionals = professionalService.listProfessionalsByBarbershopPublicId(barbershopPublicId);
        return ResponseEntity.ok(professionals);
    }

    @PutMapping("/my/{professionalPublicId}")
    public ResponseEntity<ProfessionalResponse> updateMyProfessional(@PathVariable UUID professionalPublicId, @Valid @RequestBody UpdateProfessionalRequest request) {
        ProfessionalResponse response = professionalService.updateMyProfessional(professionalPublicId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/my/{professionalPublicId}")
    public ResponseEntity<Void> deactivateMyProfessional(@PathVariable UUID professionalPublicId) {
        professionalService.deactivateMyProfessional(professionalPublicId);
        return ResponseEntity.noContent().build();
    }

}
