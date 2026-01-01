package com.alex.auth_serviceAlex.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String userId;
    private String email;
    private String username;
    private String accessToken;
    //private String tokenTyp;

    @Builder.Default
    private String tokenType = "Bearer";
}
