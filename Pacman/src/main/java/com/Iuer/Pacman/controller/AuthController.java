package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.denylist.DenyListService;          
import com.Iuer.Pacman.dto.*;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.repository.UserRepository;
import com.Iuer.Pacman.security.JwtUtil;
import io.jsonwebtoken.Claims;                             
import io.jsonwebtoken.JwtException;                       
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private DenyListService denyList;           

    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Sai username hoặc mật khẩu");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String header) {
        if (!header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing token");
        }

        try {
            Claims claims = jwtUtil.extractAllClaims(header.substring(7));

            if (claims.getId() == null) {
                return ResponseEntity.badRequest().body("Old token, please log in again.");
            }


            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            denyList.deny(claims.getId(), remaining);
            return ResponseEntity.ok("Logout successful");

        } catch (JwtException | IllegalArgumentException e) {
        return ResponseEntity.status(401).body("Invalid token");
    }
}
}