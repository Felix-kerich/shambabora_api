package com.app.shambabora.modules.marketplace.repository;

import com.app.shambabora.modules.marketplace.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCaseAndAvailableIsTrue(String q, Pageable pageable);
} 