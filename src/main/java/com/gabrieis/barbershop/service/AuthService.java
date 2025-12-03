package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.auth.AuthResponse;
import com.gabrieis.barbershop.dto.auth.LoginRequest;
import com.gabrieis.barbershop.dto.auth.RegisterRequest;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.EmailAlreadyExistsException;
import com.gabrieis.barbershop.exception.InvalidCredentialsException;
import com.gabrieis.barbershop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordHash)
                .role(UserRole.CLIENT)
                .isActive(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

}
