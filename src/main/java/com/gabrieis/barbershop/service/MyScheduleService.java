package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.schedule.CreateScheduleExceptionRequest;
import com.gabrieis.barbershop.dto.schedule.ScheduleExceptionResponse;
import com.gabrieis.barbershop.dto.schedule.UpsertWorkingHoursRequest;
import com.gabrieis.barbershop.dto.schedule.WorkingHoursResponse;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalScheduleException;
import com.gabrieis.barbershop.entity.ProfessionalWorkingHours;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.repository.ProfessionalScheduleExceptionRepository;
import com.gabrieis.barbershop.repository.ProfessionalWorkingHoursRepository;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import com.google.gson.annotations.JsonAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyScheduleService {

    private final CurrentBarbershopService currentBarbershopService;
    private final ProfessionalWorkingHoursRepository workingHoursRepository;
    private final ProfessionalScheduleExceptionRepository exceptionRepository;

    @PreAuthorize("hasRole('BARBER')")
    @Transactional
    public WorkingHoursResponse addMyWorkingHours(UpsertWorkingHoursRequest request) {
        Professional me = currentBarbershopService.requireActiveProfessional();

        Barbershop barbershop = me.getBarbershop();

        if(!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("endTime must be after startTime.");
        }

        ProfessionalWorkingHours wh = ProfessionalWorkingHours.builder()
                .barbershop(barbershop)
                .professional(me)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isActive(request.isActive() == null || request.isActive())
                .build();

        ProfessionalWorkingHours saved = workingHoursRepository.save(wh);

        return new WorkingHoursResponse(saved.getPublicId(), saved.getDayOfWeek(), saved.getStartTime(), saved.getEndTime(), saved.isActive());
    }

    @PreAuthorize("hasRole('BARBER')")
    @Transactional
    public ScheduleExceptionResponse addMyException(CreateScheduleExceptionRequest request) {
        Professional me = currentBarbershopService.requireActiveProfessional();
        Barbershop barbershop = me.getBarbershop();

        if(!request.endDateTime().isAfter(request.startDateTime())) {
            throw new BusinessException("endDateTime must be after startDateTime.");
        }

        ProfessionalScheduleException ex = ProfessionalScheduleException.builder()
                .barbershop(barbershop)
                .professional(me)
                .type(request.type())
                .startDateTime(request.startDateTime().withSecond(0).withNano(0))
                .endDateTime(request.endDateTime().withSecond(0).withNano(0))
                .notes(request.notes())
                .build();

        ProfessionalScheduleException saved = exceptionRepository.save(ex);

        return new ScheduleExceptionResponse(saved.getPublicId(), saved.getType(), saved.getStartDateTime(), saved.getEndDateTime(), saved.getNotes());
    }
}
