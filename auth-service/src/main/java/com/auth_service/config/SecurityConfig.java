package com.auth_service.config;

import com.auth_service.security.JwtAuthenticationFilter;
import com.auth_service.security.JwtUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            // 1. Deshabilitar CSRF (Crucial para APIs sin estado como REST)
            .csrf(csrf -> csrf.disable()) 
            .cors(Customizer.withDefaults())
            // 2. Configurar la autorización de peticiones
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                
                // Permite POST a las rutas locales de registro y login
                .requestMatchers(HttpMethod.POST, "/usuarios", "/login").permitAll()
                
                // Permite el "pre-vuelo" OPTIONS de CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                
                // Protege el resto de rutas
                .anyRequest().authenticated() 
            );

    // Agregar el filtro de autenticación JWT antes del UsernamePasswordAuthenticationFilter
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utilizamos BCrypt para el cifrado seguro de contraseñas
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}