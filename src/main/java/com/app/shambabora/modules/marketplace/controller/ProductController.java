package com.app.shambabora.modules.marketplace.controller;

import com.app.shambabora.common.api.ApiResponse;
import com.app.shambabora.common.api.PageResponse;
import com.app.shambabora.modules.marketplace.dto.ProductDTO;
import com.app.shambabora.modules.marketplace.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/marketplace/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(@RequestBody @Valid ProductDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Product created", productService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> update(@PathVariable Long id, @RequestBody ProductDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.update(id, request)));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        productService.setAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.get(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductDTO> products = productService.search(q, page, size);
        PageResponse<ProductDTO> body = PageResponse.<ProductDTO>builder()
                .content(products.getContent())
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
} 