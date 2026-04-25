package com.familyleague.auth.service;

import com.familyleague.auth.dto.UpdateProfileRequest;
import com.familyleague.auth.dto.UserProfileResponse;
import com.familyleague.auth.entity.User;
import com.familyleague.auth.repository.UserRepository;
import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        return UserProfileResponse.from(loadUser(username));
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest req) {
        User user = loadUser(username);

        if (req.displayName() != null) {
            user.setDisplayName(req.displayName());
        }

        if (req.avatarUrl() != null) {
            user.setAvatarUrl(req.avatarUrl());
        }

        // Password change — both fields must be supplied together
        boolean hasCurrentPw = req.currentPassword() != null && !req.currentPassword().isBlank();
        boolean hasNewPw     = req.newPassword()     != null && !req.newPassword().isBlank();

        if (hasCurrentPw || hasNewPw) {
            if (!hasCurrentPw || !hasNewPw) {
                throw new AppException(
                    "Both currentPassword and newPassword are required to change the password",
                    HttpStatus.BAD_REQUEST);
            }
            if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                throw new AppException("Current password is incorrect", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }

        return UserProfileResponse.from(userRepository.save(user));
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
