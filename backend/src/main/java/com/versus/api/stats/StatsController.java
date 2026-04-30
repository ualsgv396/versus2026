package com.versus.api.stats;

import com.versus.api.match.GameMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Stats", description = "Player statistics and rankings")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "Get the authenticated player's stats",
            parameters = @Parameter(name = "mode",
                    description = "Filter by game mode (SURVIVAL, PRECISION, BINARY_DUEL, PRECISION_DUEL, SABOTAGE). " +
                                  "Omit to get aggregated stats across all modes."),
            responses = @ApiResponse(responseCode = "200", description = "Stats returned"))
    @GetMapping("/me")
    public Object mine(@AuthenticationPrincipal UUID userId,
                       @RequestParam(required = false) GameMode mode) {
        if (mode != null) {
            return statsService.getMine(userId, mode);
        }
        return statsService.getMine(userId);
    }
}
