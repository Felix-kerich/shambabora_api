package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.FarmActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmActivityRepository extends JpaRepository<FarmActivity, Long> {
    Page<FarmActivity> findByFarmerProfileId(Long farmerProfileId, Pageable pageable);
    Page<FarmActivity> findByFarmerProfileIdAndActivityType(Long farmerProfileId, FarmActivity.ActivityType activityType, Pageable pageable);
    List<FarmActivity> findByFarmerProfileIdAndActivityType(Long farmerProfileId, FarmActivity.ActivityType activityType, Sort sort);
}
