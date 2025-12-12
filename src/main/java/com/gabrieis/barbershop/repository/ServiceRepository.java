package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findAllByBarbershop(Barbershop barbershop);

    Optional<Service> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);

    boolean existsByNameIgnoreCaseAndBarbershop(String name, Barbershop barbershop);
}
