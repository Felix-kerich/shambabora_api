package com.app.shambabora.modules.marketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, PAID, FAILED, CANCELLED

    @Column(length = 50)
    private String checkoutRequestId;

    @Column(length = 50)
    private String merchantRequestId;

    @Column(length = 20)
    private String transactionCode;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String responseDescription;

    @CreationTimestamp
    private Instant createdAt;

    private Instant paidAt;
}
