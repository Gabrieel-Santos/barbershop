package com.gabrieis.barbershop.security;

import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.BarbershopRepository;
import com.gabrieis.barbershop.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentBarbershopService {

    private final CurrentUserService currentUserService;
    private final BarbershopRepository barbershopRepository;
    private final ProfessionalRepository professionalRepository;

    public Barbershop getCurrentBarbershop() {
        User user = currentUserService.getAuthenticatedUser();

        if (user.getRole() == UserRole.OWNER || user.getRole() == UserRole.ADMIN) {
            return barbershopRepository.findByOwner(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found for current owner."));
        }

        if (user.getRole() == UserRole.BARBER) {
            Professional professional = professionalRepository.findByUserAndIsActiveTrue(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Active professional not found for current user."));

            if (professional.getBarbershop() == null) {
                throw new ResourceNotFoundException("barbershop not found for current professional.");
            }

            return professional.getBarbershop();
        }

        throw new BusinessException("Current user does not belong to a barbershop context.");
    }

    public Barbershop requireOwnerBarbershop() {
        User user = currentUserService.getAuthenticatedUser();

        if (user.getRole() != UserRole.OWNER && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Only owners can perform this action.");
        }

        return barbershopRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found for current owner."));
    }

    public Professional requireActiveProfessional() {
        User user = currentUserService.getAuthenticatedUser();

        if (user.getRole() != UserRole.BARBER) {
            throw new BusinessException("Only barbers can perform this action.");
        }

        return professionalRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("Active professional not found for current user."));
    }
}
