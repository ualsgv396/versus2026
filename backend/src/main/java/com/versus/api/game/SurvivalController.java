package com.versus.api.game;

import com.versus.api.common.dto.ErrorResponse;
import com.versus.api.game.dto.StartGameResponse;
import com.versus.api.game.dto.SurvivalAnswerRequest;
import com.versus.api.game.dto.SurvivalAnswerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Survival", description = "Singleplayer survival mode — answer questions until you run out of lives")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/game/survival")
@RequiredArgsConstructor
public class SurvivalController {

    private final GameService gameService;

    @Operation(summary = "Start a new survival session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session started, first question returned")
            })
    @PostMapping("/start")
    public StartGameResponse start(@AuthenticationPrincipal UUID userId) {
        return gameService.startSurvival(userId);
    }

    @Operation(summary = "Submit an answer for the current survival question",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Answer evaluated"),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Active session not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/answer")
    public SurvivalAnswerResponse answer(@AuthenticationPrincipal UUID userId,
                                         @Valid @RequestBody SurvivalAnswerRequest request) {
        return gameService.answerSurvival(userId, request);
    }
}
