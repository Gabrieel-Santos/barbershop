package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalService;
import com.gabrieis.barbershop.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalServiceRepository extends JpaRepository<ProfessionalService, Long> {

    List<ProfessionalService> findAllByBarbershopAndProfessional(Barbershop barbershop, Professional professional);

    List<ProfessionalService> findAllByBarbershopAndProfessionalAndIsActiveTrue(Barbershop barbershop, Professional professional);

    Optional<ProfessionalService> findByBarbershopAndProfessionalAndService(Barbershop barbershop, Professional professional, Service service);

    Optional<ProfessionalService> findByPublicIdAndBarbershop(UUID publicId, Barbershop barbershop);

    boolean existsByBarbershopAndProfessionalAndService(Barbershop barbershop, Professional professional, Service service);


}
