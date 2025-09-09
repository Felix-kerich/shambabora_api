package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.ActivityReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityReminderRepository extends JpaRepository<ActivityReminder, Long> {
    List<ActivityReminder> findByFarmActivityId(Long farmActivityId);
    List<ActivityReminder> findByFarmActivityIdIn(List<Long> farmActivityIds);
} 