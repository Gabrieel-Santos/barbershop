package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    List<Professional> findAllByBarbershop(Barbershop barbershop);

    List<Professional> findAllByBarbershopAndIsActiveTrue(Barbershop barbershop);

    Optional<Professional> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);

    Optional<Professional> findByPublicIdAndBarbershopAndIsActiveTrue(UUID publicId, Barbershop barbershop);

    boolean existsByDisplayNameIgnoreCaseAndBarbershop(String displayName, Barbershop barbershop);

    boolean existsByUserAndIsActiveTrue(User user);

    Optional<Professional> findByUserAndIsActiveTrue(User user);
}