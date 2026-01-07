package com.app.shambabora.modules.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequestDTO {
    private String moderationNotes;
}
