package com.Iuer.Pacman.security;

import com.Iuer.Pacman.denylist.DenyListService;              // MỚI
import io.jsonwebtoken.Claims;                                  // MỚI
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;             // MỚI
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DenyListService denyList;                           // MỚI

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 1. Kiểm tra chữ ký + hạn dùng (như cũ)
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.extractAllClaims(token);

                // 2. MỚI: kiểm tra JTI có trong denylist không
                try {
                    if (denyList.isDenied(claims.getId())
                            || denyList.isIssuedBeforeRevoke(claims.getSubject(), claims.getIssuedAt())) {
                        reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Token đã bị thu hồi");
                        return; // dừng, không cho đi tiếp
                    }
                } catch (DataAccessException e) {
                    // Redis lỗi: từ chối thay vì cho qua, để token đã thu hồi không lọt vào
                    reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Dịch vụ tạm thời gián đoạn");
                    return;
                }

                // 3. Token hợp lệ và chưa bị thu hồi: xác thực như cũ
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                        username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/register") || path.equals("/api/auth/login");
    }
}