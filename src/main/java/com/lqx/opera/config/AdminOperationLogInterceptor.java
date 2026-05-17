package com.lqx.opera.config;

import com.lqx.opera.entity.AdminOperationLog;
import com.lqx.opera.service.AdminOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AdminOperationLogInterceptor implements HandlerInterceptor {

    private final AdminOperationLogService adminOperationLogService;

    public AdminOperationLogInterceptor(AdminOperationLogService adminOperationLogService) {
        this.adminOperationLogService = adminOperationLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 2) {
            return;
        }

        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return;
        }
        if (path.startsWith("/api/admin/logs")) {
            return;
        }

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId((Long) request.getAttribute("userId"));
        log.setOperatorName((String) request.getAttribute("username"));
        log.setOperatorRole(role);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(path);
        log.setRequestQuery(request.getQueryString());
        log.setResponseStatus(response.getStatus());
        log.setSuccessFlag(ex == null && response.getStatus() < 400 ? 1 : 0);
        log.setCreatedTime(LocalDateTime.now());

        if (handler instanceof HandlerMethod handlerMethod) {
            log.setActionName(handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName());
        } else {
            log.setActionName(request.getMethod() + " " + path);
        }

        adminOperationLogService.save(log);
    }
}
