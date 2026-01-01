package com.alexZ.user_serviceAlex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String authUserId;
    private String username;
    private String name;
    private Integer age;
    private String phone;
    private String email;
}
