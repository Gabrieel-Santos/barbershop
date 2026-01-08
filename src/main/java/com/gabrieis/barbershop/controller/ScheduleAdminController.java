package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.schedule.CreateScheduleExceptionRequest;
import com.gabrieis.barbershop.dto.schedule.ScheduleExceptionResponse;
import com.gabrieis.barbershop.dto.schedule.UpsertWorkingHoursRequest;
import com.gabrieis.barbershop.dto.schedule.WorkingHoursResponse;
import com.gabrieis.barbershop.service.ScheduleAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/schedule/admin")
@RequiredArgsConstructor
public class ScheduleAdminController {

    private final ScheduleAdminService scheduleAdminService;

    @PostMapping("/professionals/{professionalId}/working-hours")
    public ResponseEntity<WorkingHoursResponse> upsertWorkingHours(
            @PathVariable UUID professionalId, @Valid @RequestBody UpsertWorkingHoursRequest request) {
        WorkingHoursResponse response = scheduleAdminService.upsertWorkingHours(professionalId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/professionals/{professionalId}/exceptions")
    public ResponseEntity<ScheduleExceptionResponse> createException(
            @PathVariable UUID professionalId, @Valid @RequestBody CreateScheduleExceptionRequest request) {
        ScheduleExceptionResponse response = scheduleAdminService.createException(professionalId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/exceptions/{exceptionId}")
    public ResponseEntity<Void> deleteException(@PathVariable UUID exceptionId) {
        scheduleAdminService.deleteException(exceptionId);
        return ResponseEntity.noContent().build();
    }
}
