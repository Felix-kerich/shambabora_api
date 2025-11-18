package com.app.shambabora.modules.recordskeeping.controller;

import com.app.shambabora.modules.recordskeeping.dto.MaizePatchDTO;
import com.app.shambabora.modules.recordskeeping.service.MaizePatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MaizePatchController {

    private final MaizePatchService maizePatchService;

    @Operation(summary = "Create a new patch/plot for a farmer (maize)")
    @PostMapping
    public ResponseEntity<MaizePatchDTO> createPatch(Authentication authentication,
                                                      @RequestBody MaizePatchDTO dto) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        dto.setFarmerProfileId(user.getId());
        return ResponseEntity.ok(maizePatchService.createPatch(dto));
    }

    @Operation(summary = "List patches for the authenticated farmer")
    @GetMapping
    public ResponseEntity<List<MaizePatchDTO>> listPatches(Authentication authentication) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        return ResponseEntity.ok(maizePatchService.listPatches(user.getId()));
    }

    @Operation(summary = "Get a patch by id")
    @GetMapping("/{id}")
    public ResponseEntity<MaizePatchDTO> getPatch(Authentication authentication, @PathVariable Long id) {
        com.app.shambabora.entity.User user = (com.app.shambabora.entity.User) authentication.getPrincipal();
        return ResponseEntity.ok(maizePatchService.getPatch(user.getId(), id));
    }
}
