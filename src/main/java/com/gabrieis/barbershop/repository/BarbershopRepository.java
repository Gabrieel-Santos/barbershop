package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BarbershopRepository extends JpaRepository<Barbershop, Long> {

    Optional<Barbershop> findByPublicId(UUID publicId);

    boolean existsByOwner(User owner);

    Optional<Barbershop> findByOwner(User owner);

}
