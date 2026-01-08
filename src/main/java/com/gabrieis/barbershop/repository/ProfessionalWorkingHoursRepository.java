package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalWorkingHoursRepository extends JpaRepository<ProfessionalWorkingHours, Long> {

    List<ProfessionalWorkingHours> findAllByBarbershopAndProfessionalAndDayOfWeekAndIsActiveTrue(Barbershop barbershop, Professional professional, DayOfWeek dayOfWeek);

    Optional<ProfessionalWorkingHours> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);
}
