package com.familyleague.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for updating the current user's profile.
 * All fields are optional — only non-null fields are applied.
 * To change the password, both currentPassword and newPassword must be provided.
 */
public record UpdateProfileRequest(

    @Size(min = 1, max = 100, message = "Display name must be 1-100 characters")
    String displayName,

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    String avatarUrl,

    String currentPassword,

    @Size(min = 8, message = "New password must be at least 8 characters")
    String newPassword
) {}
