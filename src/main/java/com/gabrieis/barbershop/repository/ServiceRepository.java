package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
