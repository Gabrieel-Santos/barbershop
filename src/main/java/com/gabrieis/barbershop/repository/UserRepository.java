package com.gabrieis.barbershop.repository;

import com.gabrieis.barbershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
