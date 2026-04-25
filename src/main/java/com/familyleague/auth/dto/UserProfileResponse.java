package com.familyleague.auth.dto;

import com.familyleague.auth.entity.User;

import java.util.UUID;

/** Read-only profile response (no token, no password hash). */
public record UserProfileResponse(
    UUID userId,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    String role
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getDisplayName(),
            user.getAvatarUrl(),
            user.getRole().name()
        );
    }
}
