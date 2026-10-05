package com.student.ems.employee.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class EmployeeAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        if (uri.equals("/api/employee/auth/login") || uri.equals("/api/employee/auth/status")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("EMPLOYEE_USER") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized. Employee session required.\"}");
            return false;
        }

        return true;
    }
}
