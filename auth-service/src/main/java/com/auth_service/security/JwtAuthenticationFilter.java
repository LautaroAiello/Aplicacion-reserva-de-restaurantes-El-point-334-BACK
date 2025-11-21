package com.auth_service.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // --- SOLUCIÓN AL 403 ---
        // Usamos 'endsWith' para que coincida sin importar los prefijos del Gateway
        // (ej. /usuarios o /api/auth/usuarios)
        if (
            (path.endsWith("/usuarios") && method.equals("POST")) ||
            (path.endsWith("/login") && method.equals("POST")) ||
            (path.endsWith("/admin-asignacion") && method.equals("POST")) || // <-- AÑADE ESTO
            (method.equals("OPTIONS"))
        ) {
            filterChain.doFilter(request, response);
            return;
        }
        // --- FIN DE LA SOLUCIÓN ---

        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                
                String token = header.substring(7);
                
                if (jwtUtil.isTokenValid(token)) {
                    Claims claims = jwtUtil.parseClaims(token);
                    String subject = claims.getSubject(); // Email o ID
                    
                    // --- MEJORA DE ROLES ---
                    // Leemos los roles del token (como dice tu metadata)
                    List<String> roles = claims.get("roles", List.class);
                    List<GrantedAuthority> authorities = List.of(); // Lista vacía por defecto
                    
                    if (roles != null) {
                        authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    }
                    // --- FIN MEJORA ---

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            subject, null, authorities); // <-- Ahora pasamos los roles
                            
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Guardar el ID de usuario para el controlador (como ya tenías)
                    Object usuarioId = claims.get("usuarioId");
                    request.setAttribute("usuarioId", usuarioId);
                }
            }
        } catch (Exception e) {
            // Si el token está expirado o malformado, limpiamos el contexto
            // y dejamos que la cadena continúe. Spring Security lo bloqueará con 401.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}