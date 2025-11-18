package com.app.shambabora.modules.recordskeeping.repository;

import com.app.shambabora.modules.recordskeeping.entity.MaizePatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaizePatchRepository extends JpaRepository<MaizePatch, Long> {
    List<MaizePatch> findByFarmerProfileIdOrderByYearDesc(Long farmerProfileId);
    List<MaizePatch> findByFarmerProfileIdAndYear(Long farmerProfileId, Integer year);
}
