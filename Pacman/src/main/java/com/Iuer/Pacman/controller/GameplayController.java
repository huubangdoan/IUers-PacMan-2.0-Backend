package com.Iuer.Pacman.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/game")
public class GameplayController {

    @PostMapping("/results")
    public ResponseEntity<?> submit() {
        return ResponseEntity.status(410).body(Map.of(
                "message", "Kết quả ván do server tự ghi, không nhận từ client"));
    }
}
