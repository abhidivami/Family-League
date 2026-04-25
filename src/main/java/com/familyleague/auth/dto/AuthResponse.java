package com.familyleague.auth.dto;

import java.util.UUID;

/** Response returned after successful login or registration. */
public record AuthResponse(
    UUID userId,
    String username,
    String email,
    String displayName,
    String role,
    String accessToken,
    String tokenType
) {
    public static AuthResponse of(UUID id, String username, String email,
                                   String displayName, String role, String token) {
        return new AuthResponse(id, username, email, displayName, role, token, "Bearer");
    }
}
