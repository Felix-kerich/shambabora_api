package com.app.shambabora.Controller.user;

import com.app.shambabora.dto.UpdateUserRequest;
import com.app.shambabora.dto.UserDto;
import com.app.shambabora.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDto user = authService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {
        String username = authentication.getName();
        UserDto updatedUser = authService.updateCurrentUser(username, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        authService.deleteCurrentUser(username);
        return ResponseEntity.noContent().build();
    }
} 