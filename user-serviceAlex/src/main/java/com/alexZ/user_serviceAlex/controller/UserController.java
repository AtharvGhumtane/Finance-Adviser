package com.alexZ.user_serviceAlex.controller;

import com.alexZ.user_serviceAlex.dto.UserRequest;
import com.alexZ.user_serviceAlex.dto.UserResponse;
import com.alexZ.user_serviceAlex.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     * Lazy creation: If profile doesn't exist, it will be created automatically
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile(
            @RequestHeader("X-User-Id") String authUserId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Email") String email
    ) {
        log.info("GET /api/users/profile - User ID: {}, Username: {}", authUserId, username);
        UserResponse response = userService.getMyProfile(authUserId, username, email);
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") String authUserId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Email") String email,
            @RequestBody UserRequest request
    ) {
        log.info("PUT /api/users/profile - User ID: {}, Request: {}", authUserId, request);
        UserResponse response = userService.updateProfile(authUserId, username, email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running!");
    }
}