package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopRepository extends JpaRepository<Barbershop, Long> {
}
