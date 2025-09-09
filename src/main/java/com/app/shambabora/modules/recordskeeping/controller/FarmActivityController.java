package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.modules.recordskeeping.dto.*;
import com.app.shambabora.modules.recordskeeping.service.FarmActivityService;
import com.app.shambabora.util.IcalExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farm-activities")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FarmActivityController {
    private final FarmActivityService farmActivityService;

    @Operation(summary = "Create a new farm activity")
    @PostMapping
    public ResponseEntity<FarmActivityResponse> createActivity(
            Authentication authentication,
            @Valid @RequestBody FarmActivityRequest request) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.createActivity(userId, request));
    }

    @Operation(summary = "Get a farm activity by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FarmActivityResponse> getActivity(
            Authentication authentication,
            @PathVariable Long id) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.getActivity(userId, id));
    }

    @Operation(summary = "Update a farm activity")
    @PutMapping("/{id}")
    public ResponseEntity<FarmActivityResponse> updateActivity(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FarmActivityRequest request) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.updateActivity(userId, id, request));
    }

    @Operation(summary = "Delete a farm activity")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            Authentication authentication,
            @PathVariable Long id) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        farmActivityService.deleteActivity(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List farm activities with pagination and filtering")
    @GetMapping
    public ResponseEntity<Page<FarmActivityResponse>> listActivities(
            Authentication authentication,
            @RequestParam(required = false) String activityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(farmActivityService.listActivities(userId, activityType, pageable));
    }

    @Operation(summary = "Add a reminder to a farm activity")
    @PostMapping("/{id}/reminders")
    public ResponseEntity<ActivityReminderResponse> addReminder(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ActivityReminderRequest request) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.addReminder(userId, id, request));
    }

    @Operation(summary = "List reminders for a farm activity")
    @GetMapping("/{id}/reminders")
    public ResponseEntity<List<ActivityReminderResponse>> listReminders(
            Authentication authentication,
            @PathVariable Long id) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.listReminders(userId, id));
    }

    @Operation(summary = "List upcoming reminders for the current user")
    @GetMapping("/reminders/upcoming")
    public ResponseEntity<List<ActivityReminderResponse>> listUpcomingReminders(
            Authentication authentication) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(farmActivityService.listUpcomingReminders(userId));
    }

    @Operation(summary = "Export farm activity and reminders as iCal (.ics) file")
    @GetMapping("/{id}/calendar")
    public ResponseEntity<String> exportToCalendar(
            Authentication authentication,
            @PathVariable Long id) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        Long userId = user.getId();
        FarmActivityResponse activity = farmActivityService.getActivity(userId, id);
        List<ActivityReminderResponse> reminders = farmActivityService.listReminders(userId, id);
        String ical = IcalExportUtil.generateIcal(activity, reminders);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activity-" + id + ".ics");
        return ResponseEntity.ok().headers(headers).body(ical);
    }
} 