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
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.ProfessionalRepository;
import com.gabrieis.barbershop.repository.ProfessionalScheduleExceptionRepository;
import com.gabrieis.barbershop.repository.ProfessionalWorkingHoursRepository;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleAdminService {

    private final CurrentBarbershopService currentBarbershopService;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalWorkingHoursRepository workingHoursRepository;
    private final ProfessionalScheduleExceptionRepository exceptionRepository;

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public WorkingHoursResponse upsertWorkingHours(UUID professionalPublicId, UpsertWorkingHoursRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("endTime must be after startTime.");
        }

        ProfessionalWorkingHours wh = ProfessionalWorkingHours.builder()
                .barbershop(barbershop)
                .professional(professional)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isActive(request.isActive() == null || request.isActive())
                .build();

        ProfessionalWorkingHours saved = workingHoursRepository.save(wh);

        return new WorkingHoursResponse(saved.getPublicId(), saved.getDayOfWeek(), saved.getStartTime(), saved.getEndTime(), saved.isActive());
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public ScheduleExceptionResponse createException(UUID professionalPublicId, CreateScheduleExceptionRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!request.endDateTime().isAfter(request.startDateTime())) {
            throw new BusinessException("endDateTime must be after startDateTime.");
        }

        ProfessionalScheduleException ex = ProfessionalScheduleException.builder()
                .barbershop(barbershop)
                .professional(professional)
                .type(request.type())
                .startDateTime(request.startDateTime().withSecond(0).withNano(0))
                .endDateTime(request.endDateTime().withSecond(0).withNano(0))
                .notes(request.notes())
                .build();

        ProfessionalScheduleException saved = exceptionRepository.save(ex);

        return new ScheduleExceptionResponse(saved.getPublicId(), saved.getType(), saved.getStartDateTime(), saved.getEndDateTime(), saved.getNotes());
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public void deleteException(UUID exceptionPublicId) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        ProfessionalScheduleException ex = exceptionRepository.findByPublicIdAndBarbershop(exceptionPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Exception not found for current barbershop."));

        exceptionRepository.delete(ex);
    }
}
