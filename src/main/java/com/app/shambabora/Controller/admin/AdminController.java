package com.app.shambabora.Controller.admin;

import com.app.shambabora.dto.AdminCreateUserRequest;
import com.app.shambabora.dto.UserDto;
import com.app.shambabora.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserDto createdUser = authService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        UserDto updatedUser = authService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long userId) {
        UserDto updatedUser = authService.toggleUserStatus(userId);
        return ResponseEntity.ok(updatedUser);
    }
} 