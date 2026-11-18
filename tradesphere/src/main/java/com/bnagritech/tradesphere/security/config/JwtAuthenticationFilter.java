package com.bnagritech.tradesphere.security.config;

import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.JwtService;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the "Authorization: Bearer ..." header on every request. If the token is valid and the user is still
 * active (and the token was issued after the user's last "log out everywhere"), the User becomes the logged-in
 * principal. Otherwise the request continues without a login and Spring Security answers 401.
 * This class is created by SecurityConfig, not as a @Component, so it runs only once per request.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.parse(header.substring(7).trim()).ifPresent(token ->
                    userRepository.findById(token.userId())
                            .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                            .filter(user -> user.getTokensValidFrom() == null
                                    || !token.issuedAt().isBefore(user.getTokensValidFrom()))
                            .ifPresent(user -> {
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }));
        }
        chain.doFilter(request, response);
    }
}
