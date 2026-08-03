package com.uko.eaas.identity.controller;

import com.uko.eaas.identity.dto.ApiResponse;
import com.uko.eaas.identity.model.entity.User;
import com.uko.eaas.identity.repository.UserRepository;
import com.uko.eaas.identity.service.UserAnonymizationService;
import com.uko.eaas.identity.service.UserDataExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserDataRightsController {

    private final UserDataExportService exportService;
    private final UserAnonymizationService anonymizationService;
    private final UserRepository userRepository;

    @GetMapping("/me/data-export")
    @PreAuthorize("hasAnyRole('CUSTOMER','MERCHANT')")
    public ResponseEntity<ApiResponse<String>> exportMyData(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        String json = exportService.exportUserData(userId);
        return ResponseEntity.ok(ApiResponse.success("Data export generated", json));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','MERCHANT')")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        anonymizationService.anonymizeUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @PutMapping("/me/marketing-consent")
    @PreAuthorize("hasAnyRole('CUSTOMER','MERCHANT')")
    public ResponseEntity<ApiResponse<Void>> updateMarketingConsent(
            @RequestParam Boolean consent,
            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setMarketingConsent(consent);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Marketing consent updated", null));
    }
}
