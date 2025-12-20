package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.professional.CreateProfessionalRequest;
import com.gabrieis.barbershop.dto.professional.ProfessionalResponse;
import com.gabrieis.barbershop.dto.professional.UpdateProfessionalRequest;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.*;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final CurrentBarbershopService currentBarbershopService;
    private final ServiceRepository serviceRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public ProfessionalResponse createProfessional(CreateProfessionalRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        if (professionalRepository.existsByDisplayNameIgnoreCaseAndBarbershop(request.displayName(), barbershop)) {
            throw new BusinessException("Professional with this display name already exists for this barbershop");
        }

        User linkedUser = userRepository.findByEmail(request.userEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found provided email."));


        Professional professional = Professional.builder()
                .barbershop(barbershop)
                .user(linkedUser)
                .displayName(request.displayName())
                .bio(request.bio())
                .avatarUrl(request.avatarUrl())
                .isActive(true)
                .build();

        Professional saved = professionalRepository.save(professional);

        if (linkedUser.getRole() != UserRole.BARBER) {
            linkedUser.setRole(UserRole.BARBER);
            userRepository.save(linkedUser);
        }

        List<com.gabrieis.barbershop.entity.Service> services = serviceRepository.findAllByBarbershop(barbershop);

        for (com.gabrieis.barbershop.entity.Service svc : services) {

            boolean exists = professionalServiceRepository.existsByBarbershopAndProfessionalAndService(barbershop, saved, svc);

            if(!exists) {
                com.gabrieis.barbershop.entity.ProfessionalService link = com.gabrieis.barbershop.entity.ProfessionalService.builder()
                        .barbershop(barbershop)
                        .professional(saved)
                        .service(svc)
                        .isActive(true)
                        .priceOverride(null)
                        .durationOverride(null)
                        .build();

                professionalServiceRepository.save(link);
            }
        }

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public List<ProfessionalResponse> listMyProfessionals() {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        List<Professional> professionals = professionalRepository.findAllByBarbershopAndIsActiveTrue(barbershop);

        return professionals.stream().map(this::toResponse).toList();
    }

    public List<ProfessionalResponse> listProfessionalsByBarbershopPublicId(UUID barbershopPublicId) {
        Barbershop barbershop = barbershopRepository.findByPublicId(barbershopPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found."));

        List<Professional> professionals = professionalRepository.findAllByBarbershopAndIsActiveTrue(barbershop);

        return professionals.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ProfessionalResponse updateMyProfessional(UUID professionalPublicId, UpdateProfessionalRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for current barbershop."));

        if (!professional.getDisplayName().equalsIgnoreCase(request.displayName())
                && professionalRepository.existsByDisplayNameIgnoreCaseAndBarbershop(request.displayName(), barbershop)) {
            throw new BusinessException("Professional with this display name already exists for this barbershop");
        }

        professional.setDisplayName(request.displayName());
        professional.setBio(request.bio());
        professional.setAvatarUrl(request.avatarUrl());

        if (request.isActive() != null) {
            professional.setActive(request.isActive());
        }

        Professional updated = professionalRepository.save(professional);

        if (!updated.isActive() && updated.getUser() != null) {
            normalizeUserRoleIfNoActiveProfessional(updated.getUser());
        }

        return toResponse(updated);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public void deactivateMyProfessional(UUID professionalPublicId) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        Professional professional = professionalRepository.findByPublicIdAndBarbershop(professionalPublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found for current barbershop."));

        professional.setActive(false);
        Professional saved = professionalRepository.save(professional);

        if (saved.getUser() != null) {
            normalizeUserRoleIfNoActiveProfessional(saved.getUser());
        }
    }

    private void normalizeUserRoleIfNoActiveProfessional(User user) {
        boolean stillActiveProfessional = professionalRepository.existsByUserAndIsActiveTrue(user);

        if (!stillActiveProfessional) {
            user.setRole(UserRole.CLIENT);
            userRepository.save(user);
        }
    }

    private ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(
                professional.getPublicId(),
                professional.getDisplayName(),
                professional.getBio(),
                professional.getAvatarUrl(),
                professional.isActive(),
                professional.getBarbershop() != null ? professional.getBarbershop().getPublicId() : null,
                professional.getUser() != null ? professional.getUser().getPublicId() : null
        );
    }

}
