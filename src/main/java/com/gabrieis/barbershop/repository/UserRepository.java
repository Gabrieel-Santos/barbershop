package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    
    Optional<User> findByPublicId(UUID publicId);

}
