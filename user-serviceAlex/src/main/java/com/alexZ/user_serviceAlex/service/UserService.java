package com.alexZ.user_serviceAlex.service;

import com.alexZ.user_serviceAlex.dto.UserRequest;
import com.alexZ.user_serviceAlex.dto.UserResponse;
import com.alexZ.user_serviceAlex.exception.UserNotFoundException;
import com.alexZ.user_serviceAlex.model.User;
import com.alexZ.user_serviceAlex.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user profile with lazy creation
     * If profile doesn't exist, create it automatically
     */
    @Transactional
    public UserResponse getMyProfile(String authUserId, String username, String email) {
        log.info("Fetching profile for authUserId: {}", authUserId);

        // Try to find existing profile
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    // Lazy creation: Create profile if doesn't exist
                    log.info("Profile not found for authUserId: {}. Creating new profile.", authUserId);
                    User newUser = new User();
                    newUser.setAuthUserId(authUserId);
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    return userRepository.save(newUser);
                });

        return mapToResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateProfile(String authUserId, String username, String email, UserRequest request) {
        log.info("Updating profile for authUserId: {}", authUserId);

        // Find or create user
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setAuthUserId(authUserId);
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    return newUser;
                });

        // Update fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated successfully for authUserId: {}", authUserId);

        return mapToResponse(saved);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .authUserId(user.getAuthUserId())
                .username(user.getUsername())
                .name(user.getName())
                .age(user.getAge())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }
}