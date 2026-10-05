package com.student.ems.admin.controller;

import com.student.ems.admin.model.Admin;
import com.student.ems.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.student.ems.admin.model.Employee;
import com.student.ems.admin.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(AdminAuthService adminAuthService,
                               EmployeeRepository employeeRepository,
                               PasswordEncoder passwordEncoder) {
        this.adminAuthService = adminAuthService;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            response.put("success", false);
            response.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Admin> adminOpt = adminAuthService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("ADMIN_USER", admin);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("admin", admin);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("ADMIN_USER") != null) {
            Admin admin = (Admin) session.getAttribute("ADMIN_USER");
            response.put("authenticated", true);
            response.put("admin", admin);
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-employee")
    public ResponseEntity<Map<String, Object>> verifyEmployee(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String identifier = credentials != null ? credentials.get("employeeCode") : null;
        String rawPassword = credentials != null ? credentials.get("password") : null;

        if (identifier == null || identifier.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Employee Code/Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        String search = identifier.trim();
        Optional<Employee> empOpt = employeeRepository.findByUsername(search);
        if (empOpt.isEmpty()) {
            empOpt = employeeRepository.findByEmployeeCode(search.toUpperCase());
        }
        if (empOpt.isEmpty()) {
            empOpt = employeeRepository.findByEmail(search.toLowerCase());
        }

        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            if ("INACTIVE".equalsIgnoreCase(emp.getStatus())) {
                response.put("success", false);
                response.put("message", "Your account is currently inactive. Please contact JV Group HR.");
                return ResponseEntity.status(403).body(response);
            }
            if (passwordEncoder.matches(rawPassword, emp.getPassword())) {
                response.put("success", true);
                response.put("message", "Employee authentication verified");
                response.put("employeeCode", emp.getEmployeeCode());
                response.put("fullName", emp.getFullName());
                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("message", "Invalid Employee Code or password");
        return ResponseEntity.status(401).body(response);
    }
}
