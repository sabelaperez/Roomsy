package com.roomsy.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    @Autowired
    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            TokenService tokenService,
            CustomUserDetailsService userDetailsService,
            CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract token from cookie
            var tokenOpt = cookieUtil.extractAccessToken(request);

            if (tokenOpt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenOpt.get();

            // Validate token format and signature
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if token is in Redis (not revoked)
            if (!tokenService.isAccessTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check token type
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user information
            String email = jwtUtil.extractEmail(token);

            // If user is not already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                if (jwtUtil.shouldRenewToken(token)) {
                    renewAccessToken(token, response, (CustomUserDetails) userDetails);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private void renewAccessToken(String oldToken, HttpServletResponse response, CustomUserDetails userDetails) {
        try {
            String newAccessToken = jwtUtil.generateAccessToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getRole()
            );

            tokenService.storeAccessToken(userDetails.getId(), newAccessToken);

            tokenService.revokeAccessToken(oldToken);

            response.addCookie(cookieUtil.createAccessTokenCookie(
                    newAccessToken,
                    jwtUtil.getAccessTokenExpiration()
            ));

            logger.debug("Access token renewed for user: " + userDetails.getEmail());
        } catch (Exception e) {
            logger.error("Failed to renew access token for user: " + userDetails.getEmail(), e);
        }
    }
}