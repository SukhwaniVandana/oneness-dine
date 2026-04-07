package com.oneness.dine.auth.filter;

import com.oneness.dine.auth.repository.UserSessionRepository;
import com.oneness.dine.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt      = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);
            final String role     = jwtService.extractRole(jwt);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ Check 1 — JWT signature & expiry valid
                if (!jwtService.isTokenValid(jwt, username)) {
                    log.warn("Invalid or expired JWT token for user: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                // [NEW] Added debug log to trace session lookup
                log.info("Looking up session for token prefix: {}",
                        jwt.substring(0, Math.min(20, jwt.length())));

                // ✅ Check 2 — Session still active in DB (not logged out)
                boolean sessionActive = userSessionRepository
                        .findByTokenHashAndIsActiveTrue(jwt)
                        .isPresent();

                // [NEW] Added debug log to trace result
                log.info("Session active: {}", sessionActive);

                if (!sessionActive) {
                    log.warn("Token used after logout for user: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                // ✅ Both checks passed — authenticate
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}