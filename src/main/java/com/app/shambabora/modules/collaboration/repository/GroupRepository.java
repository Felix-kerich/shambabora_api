package com.app.shambabora.modules.collaboration.repository;

import com.app.shambabora.modules.collaboration.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByNameIgnoreCase(String name);

    Page<Group> findByNameContainingIgnoreCase(String query, Pageable pageable);
} 