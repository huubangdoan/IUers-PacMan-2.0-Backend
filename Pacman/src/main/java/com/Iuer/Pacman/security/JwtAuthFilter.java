package com.Iuer.Pacman.security;

import com.Iuer.Pacman.denylist.DenyListService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final DenyListService denyList;

    public JwtAuthFilter(JwtUtil jwtUtil, DenyListService denyList) {
        this.jwtUtil = jwtUtil;
        this.denyList = denyList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = jwtUtil.extractAllClaims(token);

                if (denyList.isDenied(claims.getId())
                        || denyList.isIssuedBeforeRevoke(claims.getSubject(), claims.getIssuedAt())) {
                    reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Token đã bị thu hồi");
                    return;
                }

                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                if (username != null && role != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            username, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token không hợp lệ hoặc hết hạn -> không set authentication để security entrypoint xử lý
            } catch (DataAccessException e) {
                reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Dịch vụ xác thực tạm thời gián đoạn");
                return;
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
