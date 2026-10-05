package com.student.ems.employee.controller;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.service.EmployeeAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee/profile")
public class EmployeeProfileController {

    private final EmployeeAuthService employeeAuthService;

    public EmployeeProfileController(EmployeeAuthService employeeAuthService) {
        this.employeeAuthService = employeeAuthService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee sessionEmp = (Employee) session.getAttribute("EMPLOYEE_USER");

        Optional<Employee> empOpt = employeeAuthService.getEmployeeById(sessionEmp.getId());
        Map<String, Object> response = new HashMap<>();

        if (empOpt.isPresent()) {
            response.put("success", true);
            response.put("profile", empOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Profile not found");
            return ResponseEntity.notFound().build();
        }
    }

    public static class ProfileUpdateRequest {
        private String phone;
        private String address;
        private String gender;
        private String dob;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getDob() { return dob; }
        public void setDob(String dob) { this.dob = dob; }
    }

    @org.springframework.web.bind.annotation.PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody ProfileUpdateRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee sessionEmp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        java.sql.Date sqlDob = null;
        if (req != null && req.getDob() != null && !req.getDob().trim().isEmpty()) {
            try {
                sqlDob = java.sql.Date.valueOf(req.getDob().trim());
            } catch (Exception ignored) {}
        }

        boolean updated = employeeAuthService.updateProfile(sessionEmp.getId(), 
                req != null ? req.getPhone() : null, 
                req != null ? req.getAddress() : null, 
                req != null ? req.getGender() : null, 
                sqlDob);
        if (updated) {
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to update profile");
            return ResponseEntity.badRequest().body(response);
        }
    }

    public static class PhotoUpdateRequest {
        private String photo;
        public String getPhoto() { return photo; }
        public void setPhoto(String photo) { this.photo = photo; }
    }

    @org.springframework.web.bind.annotation.PostMapping("/photo")
    public ResponseEntity<Map<String, Object>> updatePhoto(@RequestBody PhotoUpdateRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee sessionEmp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        if (req == null || req.getPhoto() == null) {
            response.put("success", false);
            response.put("message", "Photo data is required");
            return ResponseEntity.badRequest().body(response);
        }

        boolean updated = employeeAuthService.updatePhoto(sessionEmp.getId(), req.getPhoto());
        if (updated) {
            response.put("success", true);
            response.put("message", "Photo updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to update photo");
            return ResponseEntity.badRequest().body(response);
        }
    }

    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @org.springframework.web.bind.annotation.PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody PasswordChangeRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee sessionEmp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        if (req == null || req.getCurrentPassword() == null || req.getNewPassword() == null || req.getNewPassword().trim().length() < 4) {
            response.put("success", false);
            response.put("message", "Current password and new password (min 4 characters) are required");
            return ResponseEntity.badRequest().body(response);
        }

        boolean changed = employeeAuthService.changePassword(sessionEmp.getId(), req.getCurrentPassword(), req.getNewPassword());
        if (changed) {
            response.put("success", true);
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Current password is incorrect");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
