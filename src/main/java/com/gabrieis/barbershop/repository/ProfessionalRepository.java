package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
}
