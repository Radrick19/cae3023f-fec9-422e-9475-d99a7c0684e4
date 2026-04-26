package org.example.codeconfirmapp.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.codeconfirmapp.model.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring("Bearer ".length()).trim();
            try {
                var principal = jwtService.verify(token);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal.username(),
                                null,
                                List.of(new SimpleGrantedAuthority(roleAuthority(principal.role())))
                        )
                );
            } catch (JWTVerificationException exception) {
                SecurityContextHolder.clearContext();
                throw exception;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String roleAuthority(Role role) {
        return "ROLE_" + role.name();
    }
}
