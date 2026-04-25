package com.familyleague.auth.service;

import com.familyleague.auth.dto.AuthResponse;
import com.familyleague.auth.dto.LoginRequest;
import com.familyleague.auth.dto.RegisterRequest;
import com.familyleague.auth.entity.User;
import com.familyleague.auth.repository.UserRepository;
import com.familyleague.auth.security.JwtUtil;
import com.familyleague.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Handles user registration and login, issuing JWT tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(req.username())) {
            throw new AppException("Username already taken", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new AppException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName() != null ? req.displayName() : req.username())
                .role(User.Role.ROLE_USER)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        String token = jwtUtil.generateToken(user, Map.of("role", user.getRole().name()));
        return AuthResponse.of(user.getId(), user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getRole().name(), token);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepository.findByUsernameAndDeletedAtIsNull(req.username())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.UNAUTHORIZED));

        String token = jwtUtil.generateToken(user, Map.of("role", user.getRole().name()));
        log.info("User logged in: {}", user.getUsername());
        return AuthResponse.of(user.getId(), user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getRole().name(), token);
    }
}
