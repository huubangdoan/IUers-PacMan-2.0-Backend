package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.dto.MapResponse;
import com.Iuer.Pacman.game.map.MapRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Cho frontend tải map chuẩn từ backend, để hai bên luôn dùng chung dữ liệu 
@RestController
@RequestMapping("/api/maps")
public class MapController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getMap(@PathVariable String id) {
        try {
            return ResponseEntity.ok(MapResponse.from(MapRegistry.findMap(id)));
        } catch (IllegalArgumentException exception) {
            // Frontend (api.js) đọc thông báo lỗi từ trường "message"
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy map '" + id + "'"));
        }
    }
}