package com.alexZ.api_gatewayAlexz.config;

import com.alexZ.api_gatewayAlexz.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authFilter;

    public GatewayConfig(AuthenticationFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===== PUBLIC ROUTES (No JWT Required) =====

                // Auth Service - All auth endpoints are public
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE"))


                // ===== PROTECTED ROUTES (JWT Required) =====

                // User Service - All endpoints require authentication
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE"))

                // AI Service - All endpoints require authentication
                .route("ai-service", r -> r
                        .path("/api/v1/recommendations/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://AI-RECOMMENDATION-SERVICE"))

                // Tax Optimizer - All endpoints require authentication
                .route("tax-optimizer", r -> r
                        .path("/api/v1/tax/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://TAX-OPTIMIZER-SERVICE"))

                // Credit Card Service - All endpoints require authentication
                .route("credit-card-service", r -> r
                        .path("/api/v1/credit/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://CREDIT-CARD-SERVICE"))

                .build();
    }
}