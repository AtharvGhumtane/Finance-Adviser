package com.alexZ.api_gatewayAlexz.config;

import com.alexZ.api_gatewayAlexz.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authFilter;

    public GatewayConfig(AuthenticationFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===== PUBLIC ROUTES (No JWT Required) =====

                // Auth Service - Signup
                .route("auth-signup", r -> r
                        .path("/api/auth/signup")
                        .uri("lb://AUTH-SERVICE"))

                // Auth Service - Login
                .route("auth-login", r -> r
                        .path("/api/auth/login")
                        .uri("lb://AUTH-SERVICE"))

                // Auth Service - Health Check
                .route("auth-health", r -> r
                        .path("/api/auth/health")
                        .uri("lb://AUTH-SERVICE"))


                // ===== PROTECTED ROUTES (JWT Required) =====

                // User Service - All endpoints require authentication
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE"))

                // AI Service - All endpoints require authentication
                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://AI-SERVICE"))

                .build();
    }
}