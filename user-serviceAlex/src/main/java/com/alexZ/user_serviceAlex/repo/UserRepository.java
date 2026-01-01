package com.alexZ.user_serviceAlex.repo;

import com.alexZ.user_serviceAlex.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Changed parameter from Long to String (Auth Service uses UUID)
    Optional<User> findByAuthUserId(String authUserId);

    boolean existsByAuthUserId(String authUserId);
}