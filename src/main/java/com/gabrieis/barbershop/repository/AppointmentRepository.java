package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
