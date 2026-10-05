package com.student.ems.employee.controller;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.model.LeaveRequest;
import com.student.ems.employee.service.LeaveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/leaves")
public class EmployeeLeaveController {

    private final LeaveService leaveService;

    public EmployeeLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> applyLeave(@RequestBody LeaveRequest leaveRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = leaveService.applyLeave(emp.getId(), leaveRequest);
            if (success) {
                response.put("success", true);
                response.put("message", "Leave application submitted successfully.");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to submit leave application.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting leave: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyLeaves(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        List<LeaveRequest> list = leaveService.getMyLeaves(emp.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("leaves", list);
        return ResponseEntity.ok(response);
    }
}
