package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.appointment.AppointmentResponse;
import com.gabrieis.barbershop.dto.appointment.CreateAppointmentRequest;
import com.gabrieis.barbershop.dto.appointment.UpdateAppointmentRequest;
import com.gabrieis.barbershop.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> myAppointments() {
        List<AppointmentResponse> response = appointmentService.myAppointments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<List<AppointmentResponse>> mySchedule(@RequestParam String day) {
        List<AppointmentResponse> response = appointmentService.mySchedule(LocalDate.parse(day));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barbershop")
    public ResponseEntity<List<AppointmentResponse>> barbershopSchedule(@RequestParam String day) {
        List<AppointmentResponse> response = appointmentService.barbershopSchedule(LocalDate.parse(day));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID publicId) {
        AppointmentResponse response = appointmentService.cancel(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable UUID publicId) {
        AppointmentResponse response = appointmentService.complete(publicId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable UUID publicId, @Valid @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.update(publicId, request);
        return ResponseEntity.ok(response);
    }


}
