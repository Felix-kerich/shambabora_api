package com.app.shambabora.modules.marketplace.service;

import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.marketplace.dto.ProductDTO;
import com.app.shambabora.modules.marketplace.entity.Product;
import com.app.shambabora.modules.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDTO create(ProductDTO request) {
        validate(request);
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUnit(request.getUnit());
        product.setQuantity(request.getQuantity());
        product.setAvailable(true);
        product.setSellerId(request.getSellerId());
        Product saved = productRepository.save(product);
        log.info("Product created id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getQuantity() > 0) product.setQuantity(request.getQuantity());
        if (request.getSellerId() != null) product.setSellerId(request.getSellerId());
        Product saved = productRepository.save(product);
        log.info("Product updated id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public void setAvailability(Long id, boolean available) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.setAvailable(available);
        productRepository.save(product);
        log.info("Product availability set id={} available={}", id, available);
    }

    public ProductDTO get(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public Page<ProductDTO> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = (q == null || q.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCaseAndAvailableIsTrue(q, pageable);
        return products.map(this::toDto);
    }

    public Page<ProductDTO> getSellerProducts(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable).map(this::toDto);
    }

    public Page<ProductDTO> getSellerAvailableProducts(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findBySellerIdAndAvailableIsTrueOrderByCreatedAtDesc(sellerId, pageable).map(this::toDto);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        productRepository.delete(product);
        log.info("Product deleted id={}", id);
    }

    private void validate(ProductDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }
        if (request.getPrice() == null) {
            throw new BadRequestException("price is required");
        }
        if (request.getUnit() == null || request.getUnit().isBlank()) {
            throw new BadRequestException("unit is required");
        }
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("quantity must be greater than 0");
        }
        if (request.getSellerId() == null) {
            throw new BadRequestException("sellerId is required");
        }
    }

    private ProductDTO toDto(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setUnit(entity.getUnit());
        dto.setQuantity(entity.getQuantity());
        dto.setAvailable(entity.isAvailable());
        dto.setSellerId(entity.getSellerId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
} 