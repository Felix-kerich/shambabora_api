package com.app.shambabora.modules.marketplace.service;

import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.common.exception.NotFoundException;
import com.app.shambabora.modules.marketplace.dto.OrderDTO;
import com.app.shambabora.modules.marketplace.entity.Order;
import com.app.shambabora.modules.marketplace.entity.Product;
import com.app.shambabora.modules.marketplace.repository.OrderRepository;
import com.app.shambabora.modules.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderDTO place(OrderDTO request) {
        if (request.getBuyerId() == null || request.getProductId() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("buyerId, productId and positive quantity are required");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.isAvailable()) {
            throw new BadRequestException("Product is not available");
        }
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(total);
        order.setStatus("PLACED");
        Order saved = orderRepository.save(order);
        log.info("Order placed id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public OrderDTO updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (status == null || status.isBlank()) {
            throw new BadRequestException("status is required");
        }
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        log.info("Order status updated id={} status={}", id, status);
        return toDto(saved);
    }

    public Page<OrderDTO> listByBuyer(Long buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable).map(this::toDto);
    }

    public Page<OrderDTO> listBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable).map(this::toDto);
    }

    private OrderDTO toDto(Order entity) {
        OrderDTO dto = new OrderDTO();
        dto.setId(entity.getId());
        dto.setBuyerId(entity.getBuyerId());
        dto.setSellerId(entity.getSellerId());
        dto.setProductId(entity.getProductId());
        dto.setQuantity(entity.getQuantity());
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
} 