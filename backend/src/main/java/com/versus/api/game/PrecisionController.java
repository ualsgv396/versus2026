package com.versus.api.game;

import com.versus.api.common.dto.ErrorResponse;
import com.versus.api.game.dto.PrecisionAnswerRequest;
import com.versus.api.game.dto.PrecisionAnswerResponse;
import com.versus.api.game.dto.StartGameResponse;
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

@Tag(name = "Precision", description = "Singleplayer precision mode — estimate numeric values as accurately as possible")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/game/precision")
@RequiredArgsConstructor
public class PrecisionController {

    private final GameService gameService;

    @Operation(summary = "Start a new precision session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Session started, first question returned")
            })
    @PostMapping("/start")
    public StartGameResponse start(@AuthenticationPrincipal UUID userId) {
        return gameService.startPrecision(userId);
    }

    @Operation(summary = "Submit a numeric answer for the current precision question",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Answer evaluated with score and error margin"),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Active session not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/answer")
    public PrecisionAnswerResponse answer(@AuthenticationPrincipal UUID userId,
                                          @Valid @RequestBody PrecisionAnswerRequest request) {
        return gameService.answerPrecision(userId, request);
    }
}
