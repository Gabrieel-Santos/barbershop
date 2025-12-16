package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.service.CreateServiceRequest;
import com.gabrieis.barbershop.dto.service.ServiceResponse;
import com.gabrieis.barbershop.dto.service.UpdateServiceRequest;
import com.gabrieis.barbershop.entity.Barbershop;
import com.gabrieis.barbershop.entity.Professional;
import com.gabrieis.barbershop.entity.ProfessionalService;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.BusinessException;
import com.gabrieis.barbershop.exception.ResourceNotFoundException;
import com.gabrieis.barbershop.repository.BarbershopRepository;
import com.gabrieis.barbershop.repository.ProfessionalRepository;
import com.gabrieis.barbershop.repository.ProfessionalServiceRepository;
import com.gabrieis.barbershop.repository.ServiceRepository;
import com.gabrieis.barbershop.security.CurrentBarbershopService;
import com.gabrieis.barbershop.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;
    private final CurrentBarbershopService currentBarbershopService;
    private final ProfessionalRepository professionalRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        if (serviceRepository.existsByNameIgnoreCaseAndBarbershop(request.name(), barbershop)) {
            throw new BusinessException("Service with this name already exists for this barbershop");
        }

        com.gabrieis.barbershop.entity.Service service =
                com.gabrieis.barbershop.entity.Service.builder()
                        .barbershop(barbershop)
                        .name(request.name())
                        .description(request.description())
                        .price(request.price())
                        .durationMinutes(request.durationMinutes())
                        .build();

        com.gabrieis.barbershop.entity.Service saved = serviceRepository.save(service);

        List<Professional> activeProfessionals = professionalRepository.findAllByBarbershopAndIsActiveTrue(barbershop);

        for (Professional p : activeProfessionals) {

            boolean exists = professionalServiceRepository.existsByBarbershopAndProfessionalAndService(barbershop, p, saved);

            if (!exists){
                ProfessionalService link = ProfessionalService.builder()
                        .barbershop(barbershop)
                        .professional(p)
                        .service(saved)
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
    public List<ServiceResponse> listMyServices() {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();
        List<com.gabrieis.barbershop.entity.Service> services = serviceRepository.findAllByBarbershop(barbershop);

        return services.stream().map(this::toResponse).toList();
    }

    public List<ServiceResponse> listServiceByBarbershopPublicId(UUID barbershopPublicId) {
        Barbershop barbershop = barbershopRepository.findByPublicId(barbershopPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbershop not found."));

        List<com.gabrieis.barbershop.entity.Service> services = serviceRepository.findAllByBarbershop(barbershop);

        return services.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ServiceResponse updateMyService(UUID servicePublicId, UpdateServiceRequest request) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(servicePublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for current barbershop."));

        if (!service.getName().equalsIgnoreCase(request.name())
                && serviceRepository.existsByNameIgnoreCaseAndBarbershop(request.name(), barbershop)) {
            throw new BusinessException("Service with this name already exists for this barbershop.");
        }

        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());

        com.gabrieis.barbershop.entity.Service updated = serviceRepository.save(service);

        return toResponse(updated);
    }

    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public void DeleteMyService(UUID servicePublicId) {
        Barbershop barbershop = currentBarbershopService.requireOwnerBarbershop();

        com.gabrieis.barbershop.entity.Service service = serviceRepository.findByPublicIdAndBarbershop(servicePublicId, barbershop)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for current barbershop"));

        serviceRepository.delete(service);
    }

    private ServiceResponse toResponse(com.gabrieis.barbershop.entity.Service service) {
        return new ServiceResponse(
                service.getPublicId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.getBarbershop() != null ? service.getBarbershop().getPublicId() : null
        );
    }
}
