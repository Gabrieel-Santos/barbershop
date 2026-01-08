package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalScheduleException;
import com.gabrieis.barbershop.enums.ScheduleExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalScheduleExceptionRepository extends JpaRepository<ProfessionalScheduleException, Long> {

    List<ProfessionalScheduleException> findAllByBarbershopAndProfessionalAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Barbershop barbershop, Professional professional, LocalDateTime endExclusive, LocalDateTime startInclusive
    );

    List<ProfessionalScheduleException> findAllByBarbershopAndProfessionalAndTypeAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Barbershop barbershop, Professional professional, ScheduleExceptionType type, LocalDateTime endExclusive, LocalDateTime startInclusive
    );

    Optional<ProfessionalScheduleException> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);
}
