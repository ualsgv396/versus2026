package com.versus.api.questions.dto;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String text
) {
}
