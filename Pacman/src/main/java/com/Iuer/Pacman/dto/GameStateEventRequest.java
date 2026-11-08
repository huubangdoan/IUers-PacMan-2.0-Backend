package com.Iuer.Pacman.dto;

import jakarta.validation.constraints.NotBlank;

public record GameStateEventRequest(
        @NotBlank String event,
        String value
) {
}
