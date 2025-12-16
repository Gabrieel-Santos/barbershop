package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.professionalService.LinkServiceToProfessionalRequest;
import com.gabrieis.barbershop.dto.professionalService.ProfessionalServiceResponse;
import com.gabrieis.barbershop.dto.professionalService.UpdateProfessionalServiceRequest;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalService;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.BarbershopRepository;
import com.gabrieis.barbershop.repository.ProfessionalRepository;
import com.gabrieis.barbershop.repository.ProfessionalServiceRepository;
import com.gabrieis.barbershop.repository.ServiceRepository;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessionalServicesService {

    private final ProfessionalServiceRepository professionalServiceRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;
    private final CurrentBarbershopService currentBarbershopService;

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ProfessionalServiceResponse linkServiceToProfessional(UUID professionalPublicId, LinkServiceToProfessionalRequest request) {
        if (request.servicePublicId() == null) {
            throw new BusinessException("servicePublicId is required");
        }

        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for current barbershop."));

        if (!professional.isActive()) {
            throw new BusinessException("Cannot link services to an inactive professional.");
        }

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(request.servicePublicId(), barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for current Barbershop."));

        ProfessionalService link = professionalServiceRepository.findByBarbershopAndProfessionalAndService(barbershop, professional, service)
                .orElse(null);

        if (link == null) {
            link = ProfessionalService.builder()
                    .barbershop(barbershop)
                    .professional(professional)
                    .service(service)
                    .isActive(true)
                    .priceOverride(request.priceOverride())
                    .durationOverride(request.durationOverride())
                    .build();
        } else {
            link.setActive(true);
            link.setPriceOverride(request.priceOverride());
            link.setDurationOverride(request.durationOverride());
        }

        ProfessionalService saved = professionalServiceRepository.save(link);

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public List<ProfessionalServiceResponse> listProfessionalServices(UUID professionalPublicId) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for current barbershop."));

        List<ProfessionalService> links = professionalServiceRepository.findAllByBarbershopAndProfessional(barbershop, professional);

        return links.stream().map(this::toResponse).toList();
    }

    public List<ProfessionalServiceResponse> listActiveServicesForProfessional(UUID barbershopPublicId, UUID professionalPublicId) {

        Barbershop barbershop = barbershopRepository.findByPublicId(barbershopPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found"));

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for this barbershop."));

        if (!professional.isActive()) {
            throw new BusinessException("Professional is not active");
        }

        List<ProfessionalService> links = professionalServiceRepository.findAllByBarbershopAndProfessionalAndIsActiveTrue(barbershop, professional);

        return links.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ProfessionalServiceResponse updateProfessionalService(UUID professionalServicePublicId, UpdateProfessionalServiceRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        ProfessionalService link = professionalServiceRepository.findByPublicIdAndBarbershop(professionalServicePublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService link not found for current barbershop."));

        if (request.isActive() != null) {
            link.setActive(request.isActive());
        }
        if (request.priceOverride() != null) {
            link.setPriceOverride(request.priceOverride());
        }
        if (request.durationOverride() != null) {
            link.setDurationOverride(request.durationOverride());
        }

        ProfessionalService updated = professionalServiceRepository.save(link);

        return toResponse(updated);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public void unlinkService(UUID professionalPublicId, UUID servicePublicId) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(()-> new ResourceNotFoundException("Professional not found for current barbershop."));

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(servicePublicId, barbershop)
                .orElseThrow(()-> new ResourceNotFoundException("Service not found for current barbershop."));

        ProfessionalService link = professionalServiceRepository.findByBarbershopAndProfessionalAndService(barbershop, professional, service)
                .orElseThrow(()-> new ResourceNotFoundException("Link not found."));

        link.setActive(false);
        professionalServiceRepository.save(link);
    }

    private ProfessionalServiceResponse toResponse(ProfessionalService link) {
        com.gabrieis.barbershop.entity.Service service = link.getService();

        BigDecimal price = link.getPriceOverride() != null ? link.getPriceOverride() : service.getPrice();

        Integer duration = link.getDurationOverride() != null ? link.getDurationOverride() : service.getDurationMinutes();

        return new ProfessionalServiceResponse(
                link.getPublicId(),
                link.getProfessional() != null ? link.getProfessional().getPublicId() : null,
                service != null ? service.getPublicId() : null,
                service != null ? service.getName() : null,
                price,
                duration,
                link.isActive()
        );
    }
}
