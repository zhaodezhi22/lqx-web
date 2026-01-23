package com.liuqin.opera.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.common.Result;
import com.liuqin.opera.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 放行预检请求，避免跨域 OPTIONS 触发 401
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        String auth = request.getHeader("Authorization");

        // 若接口未声明 @RequireRole，则允许匿名访问；若提供了 token，则解析并注入上下文
        if (requireRole == null) {
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    Claims claims = jwtUtils.parseToken(token);
                    Object userIdVal = claims.get("userId");
                    request.setAttribute("userId", convertToLong(userIdVal));
                    request.setAttribute("username", claims.get("username", String.class));
                    request.setAttribute("role", claims.get("role", Integer.class));
                } catch (Exception e) {
                    // token 非法时，继续匿名访问
                }
            }
            return true;
        }

        // 若接口声明了 @RequireRole，则必须携带合法 token
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供认证令牌");
            return false;
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtils.parseToken(token);
            Integer userRole = claims.get("role", Integer.class);
            Object userIdVal = claims.get("userId");
            
            request.setAttribute("userId", convertToLong(userIdVal));
            request.setAttribute("username", claims.get("username", String.class));
            request.setAttribute("role", userRole);

            boolean hasRole = false;
            for (int role : requireRole.value()) {
                if (userRole != null && userRole == role) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                writeForbidden(response, "权限不足");
                return false;
            }
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "令牌无效或已过期");
            return false;
        }
    }

    private Long convertToLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) return Long.parseLong((String) val);
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) {
        writeResponse(response, HttpStatus.UNAUTHORIZED, message);
    }
    
    private void writeForbidden(HttpServletResponse response, String message) {
        writeResponse(response, HttpStatus.FORBIDDEN, message);
    }

    private void writeResponse(HttpServletResponse response, HttpStatus status, String message) {
        try {
            response.setStatus(status.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail(status.value(), message)));
        } catch (IOException ignored) {
        }
    }
}

