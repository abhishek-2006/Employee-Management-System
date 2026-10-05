package com.student.ems.admin.controller;

import com.student.ems.admin.model.LeaveRequest;
import com.student.ems.admin.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/leaves")
public class AdminLeaveController {

    private final LeaveService leaveService;

    public AdminLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLeaveRequests(@RequestParam(required = false) String status) {
        List<LeaveRequest> list = leaveService.getAllRequests(status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("leaves", list);
        response.put("count", list.size());
        return ResponseEntity.ok(response);
    }

    public static class LeaveReviewRequest {
        private String decision; // 'APPROVED' or 'REJECTED'
        private String adminNote;

        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getAdminNote() { return adminNote; }
        public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewLeaveRequest(@PathVariable Integer id, @RequestBody LeaveReviewRequest reviewRequest) {
        Map<String, Object> response = new HashMap<>();
        if (reviewRequest == null || reviewRequest.getDecision() == null) {
            response.put("success", false);
            response.put("message", "Decision is required (APPROVED or REJECTED)");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean success = leaveService.reviewRequest(id, reviewRequest.getDecision(), reviewRequest.getAdminNote());
            if (success) {
                response.put("success", true);
                response.put("message", "Leave request has been " + reviewRequest.getDecision().toUpperCase());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Leave request not found or could not be updated");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating leave request: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
