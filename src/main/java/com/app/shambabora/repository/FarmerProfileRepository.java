package com.app.shambabora.repository;


import com.app.shambabora.entity.FarmerProfile;
import com.app.shambabora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {
    Optional<FarmerProfile> findByUser(User user);
    List<FarmerProfile> findByCounty(String county);
    Optional<FarmerProfile> findByUser_Id(Long userId);
}
