package com.dsavitskiy.orderservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtil {

    public static UUID getCurrentUserId() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object credentials = jwtAuth.getCredentials();
            if (credentials instanceof Jwt jwt) {
                String userId = jwt.getClaimAsString("sub");
                if (userId != null) {
                    return UUID.fromString(userId);
                } 
            }
        }
        throw new IllegalStateException("Cannot determine current user");
    }

    public static String getCurrentUserEmail() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object credentials = jwtAuth.getCredentials();
            if (credentials instanceof Jwt jwt) {
                String email = jwt.getClaimAsString("email");
                if (email != null) {
                    return email;
                }
            }
        }
        throw new IllegalStateException("Cannot determine current user email");
    }

    public static boolean isAdmin() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}