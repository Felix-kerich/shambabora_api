package com.app.shambabora.Controller;

import com.app.shambabora.dto.FarmerProfileRequest;
import com.app.shambabora.dto.FarmerProfileResponse;
import com.app.shambabora.entity.User;
import com.app.shambabora.service.FarmerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer-profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FarmerProfileController {
    private final FarmerProfileService farmerProfileService;

    @Operation(summary = "Get current user's farmer profile")
    @GetMapping("/me")
    public ResponseEntity<FarmerProfileResponse> getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmerProfileService.getProfile(userId));
    }

    @Operation(summary = "Update current user's farmer profile")
    @PutMapping("/me")
    public ResponseEntity<FarmerProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody FarmerProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmerProfileService.updateProfile(userId, request));
    }

    @Operation(summary = "Create farmer profile for current user")
    @PostMapping
    public ResponseEntity<FarmerProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody FarmerProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmerProfileService.createProfile(userId, request));
    }
} 