package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.service.CreateServiceRequest;
import com.gabrieis.barbershop.dto.service.ServiceResponse;
import com.gabrieis.barbershop.dto.service.UpdateServiceRequest;
import com.gabrieis.barbershop.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ServiceResponse>> listMyServices() {
        List<ServiceResponse> services = serviceService.listMyServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/barbershop/{barbershopPublicId}")
    public ResponseEntity<List<ServiceResponse>> listByBarbershop(@PathVariable UUID barbershopPublicId) {
        List<ServiceResponse> services = serviceService.listServiceByBarbershopPublicId(barbershopPublicId);
        return  ResponseEntity.ok(services);
    }

    @PutMapping("/my/{servicePublicId}")
    public ResponseEntity<ServiceResponse> updateMyService(@PathVariable UUID servicePublicId, @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = serviceService.updateMyService(servicePublicId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/my/{servicePublicId}")
    public ResponseEntity<Void> deleteMyService(@PathVariable UUID servicePublicId) {
        serviceService.DeleteMyService(servicePublicId);
        return ResponseEntity.noContent().build();
    }
}
