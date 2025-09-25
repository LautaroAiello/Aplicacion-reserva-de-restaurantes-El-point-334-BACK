package auth_service.microservice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. Deshabilitar CSRF (Crucial para APIs REST)
        http.csrf(AbstractHttpConfigurer::disable)
            // 2. Definir las reglas de acceso
            .authorizeHttpRequests(auth -> auth
                // Permite POST y GET a /usuarios sin autenticación (REGISTRO)
                .requestMatchers("/usuarios").permitAll() 
                // Requerir autenticación para cualquier otra petición (temporalmente)
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