package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.dto.GameStateEventRequest;
import com.Iuer.Pacman.service.GameStateService;
import com.Iuer.Pacman.state.GameStateResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/game/state")
public class GameStateController {
    private final GameStateService gameStateService;

    public GameStateController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @PostMapping("/session")
    public GameStateResponse startOrReset(Principal principal) {
        return gameStateService.startOrReset(principal.getName());
    }

    @GetMapping
    public GameStateResponse getState(Principal principal) {
        return gameStateService.getState(principal.getName());
    }

    @PostMapping("/events")
    public GameStateResponse applyEvent(
            Principal principal,
            @Valid @RequestBody GameStateEventRequest request
    ) {
        return gameStateService.applyEvent(principal.getName(), request.event(), request.value());
    }
}
