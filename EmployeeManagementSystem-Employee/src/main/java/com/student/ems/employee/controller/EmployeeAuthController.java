package com.student.ems.employee.controller;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.service.EmployeeAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee/auth")
public class EmployeeAuthController {

    private final EmployeeAuthService employeeAuthService;

    public EmployeeAuthController(EmployeeAuthService employeeAuthService) {
        this.employeeAuthService = employeeAuthService;
    }

    public static class LoginRequest {
        private String username;
        private String employeeCode;
        private String password;

        public String getUsername() { 
            if (username != null && !username.trim().isEmpty()) return username;
            return employeeCode;
        }
        public void setUsername(String username) { this.username = username; }
        public String getEmployeeCode() { 
            if (employeeCode != null && !employeeCode.trim().isEmpty()) return employeeCode;
            return username;
        }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        String loginIdentifier = loginRequest != null ? loginRequest.getUsername() : null;
        if (loginRequest == null || loginIdentifier == null || loginIdentifier.trim().isEmpty() || loginRequest.getPassword() == null) {
            response.put("success", false);
            response.put("message", "Username / Employee Code and password are required.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Optional<Employee> empOpt = employeeAuthService.authenticate(loginIdentifier, loginRequest.getPassword());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                HttpSession session = request.getSession(true);
                session.setAttribute("EMPLOYEE_USER", emp);

                response.put("success", true);
                response.put("message", "Login successful");
                response.put("employee", emp);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid Employee Code or password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("EMPLOYEE_USER") != null) {
            Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");
            // Refresh from DB
            Optional<Employee> fresh = employeeAuthService.getEmployeeById(emp.getId());
            if (fresh.isPresent()) {
                emp = fresh.get();
                session.setAttribute("EMPLOYEE_USER", emp);
            }
            response.put("authenticated", true);
            response.put("employee", emp);
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
}
