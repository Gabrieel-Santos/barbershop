package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.appointment.AppointmentResponse;
import com.gabrieis.barbershop.dto.appointment.CreateAppointmentRequest;
import com.gabrieis.barbershop.dto.appointment.UpdateAppointmentRequest;
import com.gabrieis.barbershop.entity.*;
import com.gabrieis.barbershop.entity.ProfessionalService;
import com.gabrieis.barbershop.enums.AppointmentStatus;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.*;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import com.gabrieis.barbershop.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarbershopRepository barbershopRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;
    private final CurrentUserService currentUserService;
    private final CurrentBarbershopService currentBarbershopService;

    @PreAuthorize("hasRole('CLIENT') or hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        User actor = currentUserService.getAuthenticatedUser();

        Barbershop barbershop = barbershopRepository.findByPublicId(request.barbershopPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found."));

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(request.professionalPublicId(), barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!professional.isActive()) {
            throw new BusinessException("Professional is not active");
        }

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(request.servicePublicId(), barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for this barbershop"));

        ProfessionalService link = professionalServiceRepository.findByBarbershopAndProfessionalAndService(barbershop, professional, service)
                .orElseThrow(() -> new BusinessException("This professional does not offer this service."));

        if (!link.isActive()) {
            throw new BusinessException("This service is not active for this professional.");
        }

        LocalDateTime start = normalizeTime(request.startTime());
        validateSlotAlignment(start, barbershop.getSlotMinutes());

        int durationMinutes = resolveDurationMinutes(service, link);
        validateDurationMultipleOfSlot(durationMinutes, barbershop.getSlotMinutes());

        LocalDateTime end = start.plusMinutes(durationMinutes);

        if (appointmentRepository.existsByProfessionalAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(professional, AppointmentStatus.SCHEDULED, end, start)) {
            throw new BusinessException("Time slot is not available for this professional.");
        }


        Appointment appt = Appointment.builder()
                .barbershop(barbershop)
                .client(actor)
                .professional(professional)
                .service(service)
                .startTime(start)
                .endTime(end)
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.notes())
                .build();

        Appointment saved = appointmentRepository.save(appt);

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('CLIENT') or hasRole('BARBER') or hasRole('OWNER') or hasRole('ADMIN')")
    public List<AppointmentResponse> myAppointments() {
        User user = currentUserService.getAuthenticatedUser();

        return appointmentRepository.findAllByClientOrderByStartTimeDesc(user).stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('BARBER')")
    public List<AppointmentResponse> mySchedule(LocalDate day) {
        Professional me = currentBarbershopService.requireActiveProfessional();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        return appointmentRepository.findAllByProfessionalAndStartTimeBetweenOrderByStartTimeAsc(me, start, end).stream()
                .map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public List<AppointmentResponse> barbershopSchedule(LocalDate day) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        return appointmentRepository.findAllByBarbershopAndStartTimeBetweenOrderByStartTimeAsc(barbershop, start, end).stream()
                .map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('CLIENT') or hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public AppointmentResponse cancel(UUID appointmentPublicId) {
        User actor = currentUserService.getAuthenticatedUser();

        Appointment appt = appointmentRepository.findByPublicId(appointmentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED appointments can be canceled.");
        }

        if (actor.getRole() == UserRole.CLIENT) {
            if (!appt.getClient().getPublicId().equals(actor.getPublicId())) {
                throw new BusinessException("You can only cancel your own appointments.");
            }
        } else {
            Barbershop my = currentBarbershopService.requireOwnerBarbershop();
            if (!appt.getBarbershop().getId().equals(my.getId())) {
                throw new BusinessException("Cannot cancel appointment from another barbershop.");
            }
        }

        appt.setStatus(AppointmentStatus.CANCELED);
        return toResponse(appointmentRepository.save(appt));
    }

    @PreAuthorize("hasRole('BARBER') or hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public AppointmentResponse complete(UUID appointmentPublicId) {
        User actor = currentUserService.getAuthenticatedUser();

        Appointment appt = appointmentRepository.findByPublicId(appointmentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED appointments can be completed.");
        }

        if (actor.getRole() == UserRole.BARBER) {
            Professional me = currentBarbershopService.requireActiveProfessional();
            if (!appt.getProfessional().getId().equals(me.getId())) {
                throw new BusinessException("You can only complete your own appointments.");
            }
        } else {
            Barbershop my = currentBarbershopService.requireOwnerBarbershop();
            if (!appt.getBarbershop().getId().equals(my.getId())) {
                throw new BusinessException("Cannot complete appointment from another barbershop.");
            }
        }

        appt.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appt));
    }

    @PreAuthorize("hasRole('BARBER') or hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public AppointmentResponse update(UUID appointmentPublicId, UpdateAppointmentRequest request) {
        User actor = currentUserService.getAuthenticatedUser();

        Appointment appt = appointmentRepository.findByPublicId(appointmentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        if (appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED appointments can be updated.");
        }

        Barbershop barbershop = appt.getBarbershop();

        if (actor.getRole() == UserRole.BARBER) {
            Professional me = currentBarbershopService.requireActiveProfessional();
            if (!appt.getProfessional().getId().equals(me.getId())) {
                throw new BusinessException("You can only update your own appointments.");
            }
        } else {
            Barbershop my = currentBarbershopService.requireOwnerBarbershop();
            if (!barbershop.getId().equals(my.getId())) {
                throw new BusinessException("Cannot update appointment from another barbershop.");
            }
        }

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(request.professionalPublicId(), barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!professional.isActive()) {
            throw new BusinessException("Professional is not active");
        }

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(request.servicePublicId(), barbershop)
                .orElseThrow(() -> new BusinessException("Service not found for this barbershop."));

        ProfessionalService link = professionalServiceRepository.findByBarbershopAndProfessionalAndService(barbershop, professional, service)
                .orElseThrow(() -> new BusinessException("This professional does not offer this service."));

        if (!link.isActive()) {
            throw new BusinessException("This service is not active for this professional.");
        }

        LocalDateTime start = normalizeTime(request.startTime());
        validateSlotAlignment(start, barbershop.getSlotMinutes());

        int durationMinutes = resolveDurationMinutes(service, link);
        validateDurationMultipleOfSlot(durationMinutes, barbershop.getSlotMinutes());

        LocalDateTime end = start.plusMinutes(durationMinutes);

        boolean hasConflict = appointmentRepository.existsByProfessionalAndStatusAndStartTimeLessThanAndEndTimeGreaterThanAndPublicIdNot(professional, AppointmentStatus.SCHEDULED, end, start, appt.getPublicId());

        if (hasConflict) {
            throw new BusinessException("Time slot is not available for this professional.");
        }

        appt.setProfessional(professional);
        appt.setService(service);
        appt.setStartTime(start);
        appt.setEndTime(end);
        appt.setNotes(request.notes());

        return toResponse(appointmentRepository.save(appt));

    }

    private int resolveDurationMinutes(com.gabrieis.barbershop.entity.Service service, ProfessionalService link) {
        Integer override = link.getDurationOverride();
        int minutes = (override != null) ? override : service.getDurationMinutes();
        if (minutes <= 0) throw new BusinessException("Invalid service duration.");
        return minutes;
    }

    private void validateSlotAlignment(LocalDateTime start, int slotMinutes) {
        int minuteOfDay = start.getHour() * 60 + start.getMinute();
        if (minuteOfDay % slotMinutes != 0) {
            throw new BusinessException("Start time must be aligned to slotMinutes=" + slotMinutes + ".");
        }
    }

    private void validateDurationMultipleOfSlot(int durationMinutes, int slotMinutes) {
        if (durationMinutes % slotMinutes != 0) {
            throw new BusinessException("Service duration must be a multiple of slotMinutes=" + slotMinutes + ".");
        }
    }

    private LocalDateTime normalizeTime(LocalDateTime dt) {
        return dt.withSecond(0).withNano(0);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getPublicId(),
                a.getBarbershop() != null ? a.getBarbershop().getPublicId() : null,
                a.getClient() != null ? a.getClient().getPublicId() : null,
                a.getProfessional() != null ? a.getProfessional().getPublicId() : null,
                a.getProfessional() != null ? a.getProfessional().getDisplayName() : null,
                a.getService() != null ? a.getService().getPublicId() : null,
                a.getService() != null ? a.getService().getName() : null,
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getNotes()

        );
    }

}
