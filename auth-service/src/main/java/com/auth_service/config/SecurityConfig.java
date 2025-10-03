package com.auth_service.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF (Crucial para APIs sin estado como REST)
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Configurar la autorización de peticiones
            .authorizeHttpRequests(authorize -> authorize
                // Permitir el acceso sin autenticación al endpoint de registro
                .requestMatchers("/usuarios").permitAll() // ✅ Permitir POST para crear usuario
                .requestMatchers("/usuarios/**").permitAll() // ✅ Permitir GET para usuarios (Feign Client lo necesita)
                
                // Otras rutas (ej: para login, si las tuvieras)
                // .requestMatchers("/auth/**").permitAll() 
                
                // Restringir cualquier otra petición:
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utilizamos BCrypt para el cifrado seguro de contraseñas
        return new BCryptPasswordEncoder();
    }
}