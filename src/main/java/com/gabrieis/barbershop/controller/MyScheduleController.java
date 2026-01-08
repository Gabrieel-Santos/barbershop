package com.gabrieis.barbershop.controller;

import com.gabrieis.barbershop.dto.schedule.CreateScheduleExceptionRequest;
import com.gabrieis.barbershop.dto.schedule.ScheduleExceptionResponse;
import com.gabrieis.barbershop.dto.schedule.UpsertWorkingHoursRequest;
import com.gabrieis.barbershop.dto.schedule.WorkingHoursResponse;
import com.gabrieis.barbershop.service.MyScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule/my")
@RequiredArgsConstructor
public class MyScheduleController {

    private final MyScheduleService myScheduleService;

    @PostMapping("/working-hours")
    public ResponseEntity<WorkingHoursResponse> addWorkingHours(@Valid @RequestBody UpsertWorkingHoursRequest request) {
        WorkingHoursResponse response = myScheduleService.addMyWorkingHours(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exceptions")
    public ResponseEntity<ScheduleExceptionResponse> addException(@Valid @RequestBody CreateScheduleExceptionRequest request) {
        ScheduleExceptionResponse response = myScheduleService.addMyException(request);
        return ResponseEntity.ok(response);
    }
}
