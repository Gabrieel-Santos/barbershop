package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.barbershop.BarbershopResponse;
import com.gabrieis.barbershop.dto.barbershop.CreateBarbershopRequest;
import com.gabrieis.barbershop.dto.barbershop.UpdateBarbershopRequest;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.EmailAlreadyExistsException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.BarbershopRepository;
import com.gabrieis.barbershop.repository.UserRepository;
import com.gabrieis.barbershop.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public BarbershopResponse createBarbershop(CreateBarbershopRequest request) {
        User user = currentUserService.getAuthenticatedUser();

        if (user.getRole() != UserRole.CLIENT && user.getRole() != UserRole.OWNER) {
            throw new BusinessException("Only clients can create a barbershop.");
        }

        if (barbershopRepository.existsByOwner(user)) {
            throw new BusinessException("User already owns a barbershop.");
        }

        if (barbershopRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already in use by another barbershop");
        }

        Barbershop barbershop = Barbershop.builder()
                .name(request.name())
                .description(request.description())
                .phone(request.phone())
                .email(request.email())
                .owner(user)
                .build();

        Barbershop saved = barbershopRepository.save(barbershop);

        if (user.getRole() == UserRole.CLIENT) {
            user.setRole(UserRole.OWNER);
            userRepository.save(user);
        }

        return toResponse(saved);
    }

    public BarbershopResponse getMyBarbershop() {
        User user = currentUserService.getAuthenticatedUser();

        Barbershop barbershop = barbershopRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found for current user."));

        return toResponse(barbershop);
    }

    public BarbershopResponse getByPublicId(UUID publicId) {
        Barbershop barbershop = barbershopRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found"));

        return toResponse(barbershop);
    }

    public BarbershopResponse updateMyBarbershop(UpdateBarbershopRequest request) {
        User user = currentUserService.getAuthenticatedUser();

        Barbershop barbershop = barbershopRepository.findByOwner(user)
                .orElseThrow(()-> new ResourceNotFoundException("Barbershop not found for current user."));

        if(!barbershop.getEmail().equals(request.email()) && barbershopRepository.existsByEmail(request.email())) {
            throw  new EmailAlreadyExistsException("Email already in use by another barbershop.");
        }

        barbershop.setName(request.name());
        barbershop.setDescription(request.description());
        barbershop.setPhone(request.phone());
        barbershop.setEmail(request.email());
        barbershop.setLogoUrl(request.logoUrl());

        Barbershop updated  = barbershopRepository.save(barbershop);

        return toResponse(updated);
    }

    private BarbershopResponse toResponse(Barbershop barbershop) {
        return new BarbershopResponse(
                barbershop.getPublicId(),
                barbershop.getName(),
                barbershop.getDescription(),
                barbershop.getPhone(),
                barbershop.getEmail(),
                barbershop.getLogoUrl(),
                barbershop.getOwner() != null ? barbershop.getOwner().getPublicId() : null
        );
    }

}
