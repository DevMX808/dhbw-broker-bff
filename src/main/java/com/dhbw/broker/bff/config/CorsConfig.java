package com.dhbw.broker.bff.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS:${cors.allowed-origins:}}") String originsCsv
    ) {
        System.out.println("CORS Config - originsCsv: " + originsCsv);

        List<String> origins = Arrays.stream(originsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        System.out.println("CORS Config - parsed origins: " + origins);

        CorsConfiguration config = new CorsConfiguration();
        if (!origins.isEmpty()) {
            config.setAllowedOrigins(origins);
            System.out.println("CORS Config - using origins: " + origins);
        } else {
            config.setAllowedOrigins(List.of(
                    "http://localhost:4200",
                    "http://localhost:8000",
                    "http://127.0.0.1:8000",
                    "https://pacific-citadel-16324-f0e736807217.herokuapp.com"  // Production
            ));
            System.out.println("CORS Config - using default localhost with MPA support");
        }

        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);

        config.setExposedHeaders(List.of(
                "Authorization",
                "WWW-Authenticate",
                "Cache-Control",
                "Content-Type"
        ));

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}