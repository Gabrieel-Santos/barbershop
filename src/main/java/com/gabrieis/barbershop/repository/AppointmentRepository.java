package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Appointment;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByPublicId(UUID publicId);

    Optional<Appointment> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);

    List<Appointment> findAllByClientOrderByStartTimeDesc(User client);

    List<Appointment> findAllByBarbershopAndStartTimeBetweenOrderByStartTimeAsc(Barbershop barbershop, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAllByProfessionalAndStartTimeBetweenOrderByStartTimeAsc(Professional professional, LocalDateTime start, LocalDateTime end);

    boolean existsByProfessionalAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(Professional professional, AppointmentStatus status, LocalDateTime end, LocalDateTime start);

    boolean existsByProfessionalAndStatusAndStartTimeLessThanAndEndTimeGreaterThanAndPublicIdNot(Professional professional, AppointmentStatus status, LocalDateTime end, LocalDateTime start, UUID excludePublicId);

}
