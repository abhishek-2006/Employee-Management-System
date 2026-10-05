package com.student.ems.admin.controller;

import com.student.ems.admin.model.Employee;
import com.student.ems.admin.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("employees", employees);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Employee> empOpt = employeeService.getEmployeeById(id);

        if (empOpt.isPresent()) {
            response.put("success", true);
            response.put("employee", empOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Employee not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Employee employee) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee created = employeeService.createEmployee(employee);
            response.put("success", true);
            response.put("message", "Employee created successfully");
            response.put("employee", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee updated = employeeService.updateEmployee(id, employee);
            response.put("success", true);
            response.put("message", "Employee updated successfully");
            response.put("employee", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public static class StatusUpdateRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Integer id, @RequestBody StatusUpdateRequest statusRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean updated = employeeService.updateStatus(id, statusRequest.getStatus());
            if (updated) {
                response.put("success", true);
                response.put("message", "Employee status updated to " + statusRequest.getStatus());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getEmployeeFullDetails(@PathVariable Integer id) {
        Map<String, Object> details = employeeService.getEmployeeFullDetails(id);
        if (details != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", details);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Employee not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Employee deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Employee not found or could not be deleted");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    public static class PhotoUpdateRequest {
        private String photo;
        public String getPhoto() { return photo; }
        public void setPhoto(String photo) { this.photo = photo; }
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<Map<String, Object>> updatePhoto(@PathVariable Integer id, @RequestBody PhotoUpdateRequest photoRequest) {
        Map<String, Object> response = new HashMap<>();
        if (photoRequest == null || photoRequest.getPhoto() == null) {
            response.put("success", false);
            response.put("message", "Photo data is required");
            return ResponseEntity.badRequest().body(response);
        }
        boolean updated = employeeService.updatePhoto(id, photoRequest.getPhoto());
        if (updated) {
            response.put("success", true);
            response.put("message", "Photo updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Employee not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    public static class ResetPasswordRequest {
        private String newPassword;
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Integer id, @RequestBody ResetPasswordRequest req) {
        Map<String, Object> response = new HashMap<>();
        if (req == null || req.getNewPassword() == null || req.getNewPassword().trim().length() < 4) {
            response.put("success", false);
            response.put("message", "New password must be at least 4 characters long");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean reset = employeeService.resetPassword(id, req.getNewPassword());
            if (reset) {
                response.put("success", true);
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
