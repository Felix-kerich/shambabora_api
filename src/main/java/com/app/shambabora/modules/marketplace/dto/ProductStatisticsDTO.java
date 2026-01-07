package com.app.shambabora.modules.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product statistics DTO for admin dashboard.
 * Provides summary statistics about products in the marketplace.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDTO {
    
    /**
     * Total number of products
     */
    private Long totalProducts;
    
    /**
     * Number of available products
     */
    private Long availableProducts;
    
    /**
     * Number of unavailable products
     */
    private Long unavailableProducts;
    
    /**
     * Number of active sellers
     */
    private Long activeSellers;
    
    /**
     * Average product price
     */
    private Double averagePrice;
    
    /**
     * Highest product price
     */
    private Double maxPrice;
    
    /**
     * Lowest product price
     */
    private Double minPrice;
    
    /**
     * Total inventory value
     */
    private Double totalInventoryValue;
    
    /**
     * Number of products added in the last 7 days
     */
    private Long recentProductsCount;
    
    /**
     * Top product category
     */
    private String topCategory;
    
    /**
     * Number of products in top category
     */
    private Long topCategoryCount;
}
