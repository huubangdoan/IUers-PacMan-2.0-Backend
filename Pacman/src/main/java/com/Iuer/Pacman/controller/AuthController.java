package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.denylist.DenyListService;
import com.Iuer.Pacman.dto.AuthResponse;
import com.Iuer.Pacman.dto.LoginRequest;
import com.Iuer.Pacman.dto.RegisterRequest;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.security.JwtUtil;
import com.Iuer.Pacman.security.LoginAttemptService;
import com.Iuer.Pacman.service.UserIndexService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserIndexService userIndexService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DenyListService denyList;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            UserIndexService userIndexService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            DenyListService denyList,
            LoginAttemptService loginAttemptService
    ) {
        this.userIndexService = userIndexService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.denyList = denyList;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username và password không được để trống");
        }

        if (userIndexService.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("USER");

        userIndexService.save(user);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body("Vui lòng cung cấp username và password");
        }

        String username = req.getUsername().trim();

        if (loginAttemptService.isBlocked(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Tài khoản tạm thời bị khóa 15 phút do nhập sai mật khẩu quá nhiều lần");
        }

        User user = userIndexService.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai username hoặc mật khẩu");
        }

        loginAttemptService.loginSucceeded(username);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header == null || !header.startsWith("Bearer ")) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
