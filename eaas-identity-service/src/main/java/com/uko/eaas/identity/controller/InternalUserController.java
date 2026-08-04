package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.UserContactResponse;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal API for service-to-service communication.
 * Exposes user contact details (email/phone) to the Communication Service
 * for resolving notification recipients. Secured by internal network isolation.
 */
@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/{userId}/contact")
    public ResponseEntity<UserContactResponse> getContact(@PathVariable UUID userId) {
        log.debug("Internal request for user contact details: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "User not found: " + userId));

        UserContactResponse response = UserContactResponse.builder()
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .build();

        return ResponseEntity.ok(response);
    }
}