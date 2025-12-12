package com.gabrieis.barbershop.service;

import com.gabrieis.barbershop.dto.auth.AuthResponse;
import com.gabrieis.barbershop.dto.auth.LoginRequest;
import com.gabrieis.barbershop.dto.auth.RegisterRequest;
import com.gabrieis.barbershop.entity.User;
import com.gabrieis.barbershop.enums.UserRole;
import com.gabrieis.barbershop.exception.EmailAlreadyExistsException;
import com.gabrieis.barbershop.exception.InvalidCredentialsException;
import com.gabrieis.barbershop.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.oauth.google.client-id}")
    private String googleClientId;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
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

    public AuthResponse loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new InvalidCredentialsException("Invalid Google token");
            }

            GoogleIdToken.Payload payload =idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            Boolean emailVerified = (Boolean) payload.getEmailVerified();

            if (emailVerified == null || !emailVerified) {
                throw new InvalidCredentialsException("Google email not verified");
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .name(name != null ? name : email)
                                .email(email)
                                .phone(null)
                                .passwordHash(null)
                                .role(UserRole.CLIENT)
                                .isActive(true)
                                .build();
                        return userRepository.save(newUser);
                    });

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponse(accessToken, refreshToken);

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid Google token");
        }
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            String subject = jwtService.extractSubject(refreshToken);
            UUID publicId = UUID.fromString(subject);

            User user = userRepository.findByPublicId(publicId)
                    .orElseThrow(()-> new InvalidCredentialsException("Invalid refresh token"));

            if (!jwtService.isTokenValid(refreshToken, user) || !jwtService.isRefreshToken(refreshToken)) {
                throw new InvalidCredentialsException("Invalid refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponse(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

}
