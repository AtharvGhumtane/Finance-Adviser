package com.alex.auth_serviceAlex.service;

import com.alex.auth_serviceAlex.dto.AuthResponse;
import com.alex.auth_serviceAlex.dto.LoginRequest;
import com.alex.auth_serviceAlex.dto.SignUpRequest;
import com.alex.auth_serviceAlex.exception.InvalidCredentialsException;
import com.alex.auth_serviceAlex.exception.UserAlreadyExistsException;
import com.alex.auth_serviceAlex.model.User;
import com.alex.auth_serviceAlex.repo.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse signUp(@Valid SignUpRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + request.getUsername() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername() != null ? request.getUsername() : request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .accountLocked(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // Generate JWT token
        String token = jwtService.generateToken(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    /**
     * Authenticate user and generate token
     */
    public AuthResponse login(@Valid LoginRequest request) {
        log.info("Attempting login for: {}", request.getEmailOrUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmailOrUsername(),
                            request.getPassword()
                    )
            );

            // Fetch user details
            User user = userRepository.findByEmailOrUsername(
                    request.getEmailOrUsername(),
                    request.getEmailOrUsername()
            ).orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

            // Check if account is active
            if (!user.getIsActive()) {
                throw new InvalidCredentialsException("Account is deactivated");
            }

            if (user.getAccountLocked()) {
                throw new InvalidCredentialsException("Account is locked");
            }

            log.info("User authenticated successfully: {}", user.getUserId());

            // Generate JWT token
            String token = jwtService.generateToken(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail()
            );

            return AuthResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .accessToken(token)
                    .tokenType("Bearer")
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for: {}", request.getEmailOrUsername());
            throw new InvalidCredentialsException("Invalid email/username or password");
        }
    }

    /**
     * Validate JWT token (for Gateway to call)
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return username != null && jwtService.isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}