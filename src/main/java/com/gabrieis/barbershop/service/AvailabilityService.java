package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.availability.AvailabilityResponse;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalService;
import com.gabrieis.barbershop.enums.AppointmentStatus;
import com.gabrieis.barbershop.enums.ScheduleExceptionType;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final BarbershopRepository barbershopRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;
    private final ProfessionalWorkingHoursRepository workingHoursRepository;
    private final ProfessionalScheduleExceptionRepository exceptionRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityResponse getAvailability(UUID barbershopPublicId, UUID professionalPublicId, LocalDate date, UUID servicePublicIdOrNull) {

        Barbershop barbershop = barbershopRepository.findByPublicId(barbershopPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found."));

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!professional.isActive()) {
            throw new BusinessException("Professional is not active.");
        }

        int slot = (barbershop.getSlotMinutes() != null) ? barbershop.getSlotMinutes() : 20;

        Integer durationMinutes = null;

        if (servicePublicIdOrNull != null) {
            com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(servicePublicIdOrNull, barbershop)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found for this barbershop"));

            ProfessionalService link = professionalServiceRepository.findByBarbershopAndProfessionalAndService(barbershop, professional, service)
                    .orElseThrow(() -> new BusinessException("This professional does not offer this service."));

            if (!link.isActive()) throw new BusinessException("This service is not active for this professional.");

            durationMinutes = resolveDurationMinutes(service, link);

            if (durationMinutes % slot != 0) {
                throw new BusinessException("Service duration must be a multiple of slotMinutes=" + slot + ".");
            }
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<TimeInterval> intervals = new ArrayList<>();
        DayOfWeek dow = date.getDayOfWeek();

        var workBlocks = workingHoursRepository.findAllByBarbershopAndProfessionalAndDayOfWeekAndIsActiveTrue(barbershop, professional, dow);

        for (var wh : workBlocks) {
            LocalDateTime s = LocalDateTime.of(date, wh.getStartTime());
            LocalDateTime e = LocalDateTime.of(date, wh.getEndTime());
            if (e.isAfter(s)) intervals.add(new TimeInterval(s, e));
        }

        var openExceptions = exceptionRepository.findAllByBarbershopAndProfessionalAndTypeAndStartDateTimeLessThanAndEndDateTimeGreaterThan(barbershop, professional, ScheduleExceptionType.OPEN, dayEnd, dayStart);

        for (var ex : openExceptions) {
            TimeInterval clipped = clipToDay(ex.getStartDateTime(), ex.getEndDateTime(), dayStart, dayEnd);
            if (clipped != null) intervals.add(clipped);
        }

        intervals = mergeIntervals(intervals);

        var blockExceptions = exceptionRepository.findAllByBarbershopAndProfessionalAndTypeAndStartDateTimeLessThanAndEndDateTimeGreaterThan(barbershop, professional, ScheduleExceptionType.BLOCK, dayEnd, dayStart);

        for (var ex : blockExceptions) {
            TimeInterval clipped = clipToDay(ex.getStartDateTime(), ex.getEndDateTime(), dayStart, dayEnd);
            if (clipped != null) intervals = subtractIntervalList(intervals, clipped);
        }

        var appts = appointmentRepository.findAllByProfessionalAndStatusAndStartTimeBetweenOrderByStartTimeAsc(professional, AppointmentStatus.SCHEDULED, dayStart, dayEnd);

        for (var a : appts) {
            TimeInterval busy = clipToDay(a.getStartTime(), a.getEndTime(), dayStart, dayEnd);
            if (busy != null) intervals = subtractIntervalList(intervals, busy);
        }

        List<LocalDateTime> slots = new ArrayList<>();
        int dur = (durationMinutes != null) ? durationMinutes : slot;

        for (TimeInterval free : intervals) {
            LocalDateTime cursor = ceilToSlot(free.start, slot);

            while (!cursor.plusMinutes(dur).isAfter(free.end)) {
                if (isAligned(cursor, slot)) {
                    slots.add(cursor);
                }
                cursor = cursor.plusMinutes(slot);
            }
        }

        return new AvailabilityResponse(barbershopPublicId, professionalPublicId, date, slot, durationMinutes, slots);
    }

    public boolean isSlotAvailable(Barbershop barbershop, Professional professional, com.gabrieis.barbershop.entity.Service service, LocalDateTime start) {
        if (!professional.isActive()) return false;

        UUID serviceId = (service != null) ? service.getPublicId() : null;

        AvailabilityResponse availability  = getAvailability(barbershop.getPublicId(), professional.getPublicId(), start.toLocalDate(), serviceId);

        return availability.availableStartTimes().contains(start.withSecond(0).withNano(0));
    }

    private int resolveDurationMinutes(com.gabrieis.barbershop.entity.Service service, ProfessionalService link) {
        Integer override = link.getDurationOverride();
        int minutes = (override != null) ? override : service.getDurationMinutes();
        if (minutes <= 0) throw new BusinessException("Invalid service duration.");
        return minutes;
    }

    private boolean isAligned(LocalDateTime dt, int slot) {
        int minuteOfDay = dt.getHour() * 60 + dt.getMinute();
        return minuteOfDay % slot == 0 && dt.getSecond() == 0 && dt.getNano() == 0;
    }

    private LocalDateTime ceilToSlot(LocalDateTime dt, int slot) {
        dt = dt.withSecond(0).withNano(0);
        int minuteOfDay = dt.getHour() * 60 + dt.getMinute();
        int mod = minuteOfDay % slot;
        if (mod == 0) return dt;
        int add = slot - mod;
        return dt.plusMinutes(add);
    }

    private TimeInterval clipToDay(LocalDateTime s, LocalDateTime e, LocalDateTime dayStart, LocalDateTime dayEnd) {
        LocalDateTime start = s.isBefore(dayStart) ? dayStart : s;
        LocalDateTime end = e.isAfter(dayEnd) ? dayEnd : e;
        if (!end.isAfter(start)) return null;
        return new TimeInterval(start, end);
    }

    private List<TimeInterval> mergeIntervals(List<TimeInterval> in) {
        if (in.isEmpty()) return in;

        in.sort(Comparator.comparing(t -> t.start));
        List<TimeInterval> out = new ArrayList<>();

        TimeInterval cur = in.get(0);
        for (int i = 1; i < in.size(); i++) {
            TimeInterval nxt = in.get(1);
            if (!nxt.start.isAfter(cur.end)) {
                cur = new TimeInterval(cur.start, max(cur.end, nxt.end));
            } else {
                out.add(cur);
                cur = nxt;
            }
        }
        out.add(cur);
        return out;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private List<TimeInterval> subtractIntervalList(List<TimeInterval> base, TimeInterval cut) {
        List<TimeInterval> out = new ArrayList<>();
        for (TimeInterval t : base) {
            out.addAll(subtractOne(t, cut));
        }
        return out;
    }

    private List<TimeInterval> subtractOne(TimeInterval a, TimeInterval b) {
        if (b.end.compareTo(a.start) <= 0 || b.start.compareTo(b.end) >= 0) {
            return List.of(a);
        }

        if (b.start.compareTo(a.start) <= 0 && b.end.compareTo(a.end) >= 0) {
            return List.of();
        }

        if (b.start.compareTo(a.start) <= 0) {
            return List.of(new TimeInterval(b.end, a.end));
        }

        if (b.end.compareTo(a.end) >= 0) {
            return List.of(new TimeInterval(a.start, b.start));
        }

        return List.of(new TimeInterval(a.start, b.start), new TimeInterval(b.end, a.end));
    }


    private record TimeInterval(LocalDateTime start, LocalDateTime end) {
    }

}
