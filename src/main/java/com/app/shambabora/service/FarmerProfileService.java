package com.app.shambabora.service;

import com.app.shambabora.dto.FarmerProfileRequest;
import com.app.shambabora.dto.FarmerProfileResponse;
import com.app.shambabora.entity.FarmerProfile;
import com.app.shambabora.entity.User;
import com.app.shambabora.repository.FarmerProfileRepository;
import com.app.shambabora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FarmerProfileService {
    private final FarmerProfileRepository farmerProfileRepository;
    private final UserRepository userRepository;

    public FarmerProfileResponse getProfile(Long userId) {
        FarmerProfile profile = farmerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
        return toResponse(profile);
    }

    @Transactional
    public FarmerProfileResponse updateProfile(Long userId, FarmerProfileRequest request) {
        FarmerProfile profile = farmerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
        updateEntity(profile, request);
        farmerProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public FarmerProfileResponse createProfile(Long userId, FarmerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        FarmerProfile profile = new FarmerProfile();
        profile.setUser(user);
        updateEntity(profile, request);
        farmerProfileRepository.save(profile);
        return toResponse(profile);
    }

    private void updateEntity(FarmerProfile profile, FarmerProfileRequest request) {
        profile.setFarmName(request.getFarmName());
        profile.setCounty(request.getCounty());
        profile.setLocation(request.getLocation());
        profile.setFarmSize(request.getFarmSize());
        profile.setFarmDescription(request.getFarmDescription());
        profile.setAlternatePhone(request.getAlternatePhone());
        profile.setPostalAddress(request.getPostalAddress());
    }

    private FarmerProfileResponse toResponse(FarmerProfile profile) {
        FarmerProfileResponse resp = new FarmerProfileResponse();
        resp.setId(profile.getId());
        resp.setUserId(profile.getUser().getId());
        resp.setFarmName(profile.getFarmName());
        resp.setCounty(profile.getCounty());
        resp.setLocation(profile.getLocation());
        resp.setFarmSize(profile.getFarmSize());
        resp.setFarmDescription(profile.getFarmDescription());
        resp.setAlternatePhone(profile.getAlternatePhone());
        resp.setPostalAddress(profile.getPostalAddress());
        resp.setCreatedAt(profile.getCreatedAt());
        resp.setUpdatedAt(profile.getUpdatedAt());
        return resp;
    }
} 