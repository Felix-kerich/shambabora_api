package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.*;
import com.app.shambabora.modules.recordskeeping.entity.ActivityReminder;
import com.app.shambabora.modules.recordskeeping.entity.FarmActivity;
import com.app.shambabora.modules.recordskeeping.repository.ActivityReminderRepository;
import com.app.shambabora.modules.recordskeeping.repository.MaizePatchRepository;
import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import com.app.shambabora.modules.recordskeeping.repository.FarmActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmActivityService {
    private final FarmActivityRepository farmActivityRepository;
    private final ActivityReminderRepository activityReminderRepository;
    private final MaizePatchRepository maizePatchRepository;

    @Transactional
    public FarmActivityResponse createActivity(Long userId, FarmActivityRequest request) {
    // validate patch and get patchName
    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));

    FarmActivity activity = FarmActivity.builder()
                .farmerProfileId(userId)
                .activityType(FarmActivity.ActivityType.valueOf(request.getActivityType().toUpperCase()))
                .cropType(request.getCropType())
                .activityDate(request.getActivityDate())
        .patchId(request.getPatchId())
        .patchName(patch.getName())
                .description(request.getDescription())
                .areaSize(request.getAreaSize())
                .units(request.getUnits())
                .yield(request.getYield())
                .cost(request.getCost())
                .productUsed(request.getProductUsed())
                .applicationRate(request.getApplicationRate())
                .weatherConditions(request.getWeatherConditions())
                .soilConditions(request.getSoilConditions())
                .notes(request.getNotes())
                .location(request.getLocation())
                .laborHours(request.getLaborHours())
                .equipmentUsed(request.getEquipmentUsed())
                .laborCost(request.getLaborCost())
                .equipmentCost(request.getEquipmentCost())
                .build();
        FarmActivity saved = farmActivityRepository.save(activity);
        FarmActivityResponse response = toResponse(saved);
        if (isYieldRecording(saved)) {
            addYieldAnalytics(response, saved.getFarmerProfileId(), saved);
        }
        return response;
    }

    public FarmActivityResponse getActivity(Long userId, Long activityId) {
        FarmActivity activity = getOwnedActivity(userId, activityId);
        return toResponse(activity);
    }

    @Transactional
    public FarmActivityResponse updateActivity(Long userId, Long activityId, FarmActivityRequest request) {
        FarmActivity activity = getOwnedActivity(userId, activityId);
    // validate patch
    MaizePatch patch = maizePatchRepository.findById(request.getPatchId())
        .filter(p -> p.getFarmerProfileId().equals(userId))
        .orElseThrow(() -> new EntityNotFoundException("Patch not found or access denied"));
        activity.setActivityType(FarmActivity.ActivityType.valueOf(request.getActivityType().toUpperCase()));
        activity.setCropType(request.getCropType());
        activity.setActivityDate(request.getActivityDate());
    activity.setPatchId(request.getPatchId());
    activity.setPatchName(patch.getName());
        activity.setDescription(request.getDescription());
        activity.setAreaSize(request.getAreaSize());
        activity.setUnits(request.getUnits());
        activity.setYield(request.getYield());
        activity.setCost(request.getCost());
        activity.setProductUsed(request.getProductUsed());
        activity.setApplicationRate(request.getApplicationRate());
        activity.setWeatherConditions(request.getWeatherConditions());
        activity.setSoilConditions(request.getSoilConditions());
        activity.setNotes(request.getNotes());
        activity.setLocation(request.getLocation());
        activity.setLaborHours(request.getLaborHours());
        activity.setEquipmentUsed(request.getEquipmentUsed());
        activity.setLaborCost(request.getLaborCost());
        activity.setEquipmentCost(request.getEquipmentCost());
        FarmActivity saved = farmActivityRepository.save(activity);
        FarmActivityResponse response = toResponse(saved);
        if (isYieldRecording(saved)) {
            addYieldAnalytics(response, saved.getFarmerProfileId(), saved);
        }
        return response;
    }

    @Transactional
    public void deleteActivity(Long userId, Long activityId) {
        FarmActivity activity = getOwnedActivity(userId, activityId);
        farmActivityRepository.delete(activity);
    }

    public Page<FarmActivityResponse> listActivities(Long userId, String activityType, Pageable pageable) {
        Page<FarmActivity> page;
        if (activityType != null) {
            page = farmActivityRepository.findByFarmerProfileIdAndActivityType(
                    userId, FarmActivity.ActivityType.valueOf(activityType.toUpperCase()), pageable);
        } else {
            page = farmActivityRepository.findByFarmerProfileId(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public ActivityReminderResponse addReminder(Long userId, Long activityId, ActivityReminderRequest request) {
        FarmActivity activity = getOwnedActivity(userId, activityId);
        ActivityReminder reminder = ActivityReminder.builder()
                .farmActivityId(activity.getId())
                .reminderDateTime(request.getReminderDateTime())
                .message(request.getMessage())
                .repeatInterval(ActivityReminder.RepeatInterval.valueOf(
                        request.getRepeatInterval() == null ? "NONE" : request.getRepeatInterval().toUpperCase()))
                .build();
        ActivityReminder saved = activityReminderRepository.save(reminder);
        return toReminderResponse(saved);
    }

    public List<ActivityReminderResponse> listReminders(Long userId, Long activityId) {
        FarmActivity activity = getOwnedActivity(userId, activityId);
        return activityReminderRepository.findByFarmActivityId(activity.getId())
                .stream().map(this::toReminderResponse).collect(Collectors.toList());
    }

    public List<ActivityReminderResponse> listUpcomingReminders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> activityIds = farmActivityRepository.findByFarmerProfileId(userId, Pageable.unpaged())
                .getContent().stream().map(FarmActivity::getId).toList();
        return activityReminderRepository.findByFarmActivityIdIn(activityIds)
                .stream()
                .filter(r -> r.getReminderDateTime().isAfter(now))
                .map(this::toReminderResponse)
                .collect(Collectors.toList());
    }

    private FarmActivity getOwnedActivity(Long userId, Long activityId) {
        FarmActivity activity = farmActivityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Farm activity not found"));
        if (!activity.getFarmerProfileId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return activity;
    }

    private FarmActivityResponse toResponse(FarmActivity activity) {
        FarmActivityResponse dto = new FarmActivityResponse();
        dto.setPatchId(activity.getPatchId());
        dto.setPatchName(activity.getPatchName());
        dto.setId(activity.getId());
        dto.setActivityType(activity.getActivityType().name());
        dto.setCropType(activity.getCropType());
        dto.setActivityDate(activity.getActivityDate());
        dto.setDescription(activity.getDescription());
        dto.setAreaSize(activity.getAreaSize());
        dto.setUnits(activity.getUnits());
        dto.setYield(activity.getYield());
        dto.setCost(activity.getCost());
        dto.setProductUsed(activity.getProductUsed());
        dto.setApplicationRate(activity.getApplicationRate());
        dto.setWeatherConditions(activity.getWeatherConditions());
        dto.setSoilConditions(activity.getSoilConditions());
        dto.setNotes(activity.getNotes());
        dto.setLocation(activity.getLocation());
        dto.setLaborHours(activity.getLaborHours());
        dto.setEquipmentUsed(activity.getEquipmentUsed());
        dto.setLaborCost(activity.getLaborCost());
        dto.setEquipmentCost(activity.getEquipmentCost());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }

    private ActivityReminderResponse toReminderResponse(ActivityReminder reminder) {
        ActivityReminderResponse dto = new ActivityReminderResponse();
        dto.setId(reminder.getId());
        dto.setActivityId(reminder.getFarmActivityId());
        dto.setReminderDateTime(reminder.getReminderDateTime());
        dto.setMessage(reminder.getMessage());
        dto.setRepeatInterval(reminder.getRepeatInterval().name());
        dto.setCreatedAt(reminder.getCreatedAt());
        dto.setUpdatedAt(reminder.getUpdatedAt());
        return dto;
    }

    private boolean isYieldRecording(FarmActivity activity) {
        return activity.getActivityType() == FarmActivity.ActivityType.YIELD_RECORDING ||
               activity.getActivityType() == FarmActivity.ActivityType.HARVESTING;
    }

    private void addYieldAnalytics(FarmActivityResponse response, Long farmerProfileId, FarmActivity currentActivity) {
        List<FarmActivity> previousYields = farmActivityRepository.findByFarmerProfileIdAndActivityType(
                farmerProfileId, FarmActivity.ActivityType.YIELD_RECORDING, Sort.by(Sort.Direction.DESC, "activityDate"));
        previousYields = previousYields.stream()
                .filter(a -> a.getCropType().equalsIgnoreCase(currentActivity.getCropType()) &&
                        !a.getId().equals(currentActivity.getId()) &&
                        a.getActivityDate().isBefore(currentActivity.getActivityDate()) &&
                        a.getYield() != null)
                .toList();
        if (!previousYields.isEmpty() && currentActivity.getYield() != null) {
            FarmActivity last = previousYields.get(0);
            double prevYield = last.getYield();
            double currYield = currentActivity.getYield();
            double change = currYield - prevYield;
            double percent = prevYield == 0 ? 0 : (change / prevYield) * 100;
            response.setPercentageChange(percent);
            if (change > 0) {
                response.setYieldTrend("INCREASE");
            } else if (change < 0) {
                response.setYieldTrend("DECREASE");
            } else {
                response.setYieldTrend("STEADY");
            }
            StringBuilder reasons = new StringBuilder();
            if (Math.abs(percent) > 10) {
                if (currentActivity.getCost() != null && last.getCost() != null && currentActivity.getCost().compareTo(last.getCost()) > 0) {
                    reasons.append("Higher input cost this year. ");
                }
                if (currentActivity.getAreaSize() != null && last.getAreaSize() != null && !currentActivity.getAreaSize().equals(last.getAreaSize())) {
                    reasons.append("Area size changed. ");
                }
            }
            response.setPossibleReasons(reasons.length() > 0 ? reasons.toString() : null);
        }
    }
}
