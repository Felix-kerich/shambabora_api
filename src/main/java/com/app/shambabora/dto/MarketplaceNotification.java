package com.app.shambabora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceNotification {
    private Long userId;
    private String notificationType;
    private String message;
    private Long relatedEntityId;
}
