package com.alexZ.user_serviceAlex.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Changed from Long to String because Auth Service uses UUID
    @Column(unique = true, nullable = false)
    private String authUserId;

    private String username;

    private String name;

    private Integer age;

    private String phone;

    @Column(nullable = false)
    private String email;
}