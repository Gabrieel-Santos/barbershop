package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.barbershop.BarbershopResponse;
import com.gabrieis.barbershop.dto.barbershop.CreateBarbershopRequest;
import com.gabrieis.barbershop.dto.barbershop.UpdateBarbershopRequest;
import com.gabrieis.barbershop.service.BarbershopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/barbershops")
@RequiredArgsConstructor
public class BarbershopController {

    private final BarbershopService barbershopService;

    @PostMapping
    public ResponseEntity<BarbershopResponse> create(@Valid @RequestBody CreateBarbershopRequest request) {
        BarbershopResponse response = barbershopService.createBarbershop(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<BarbershopResponse> getMyBarbershop() {
        BarbershopResponse response = barbershopService.getMyBarbershop();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{publicId}")
    public ResponseEntity<BarbershopResponse> getByPublicId(@PathVariable UUID publicId) {
        BarbershopResponse response = barbershopService.getByPublicId(publicId);
        return  ResponseEntity.ok(response);
    }

    @PutMapping("/my")
    public ResponseEntity<BarbershopResponse> updateMyBarbershop(@Valid @RequestBody UpdateBarbershopRequest request) {
        BarbershopResponse response = barbershopService.updateMyBarbershop(request);
        return ResponseEntity.ok(response);
    }
}
