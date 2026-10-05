package com.student.ems.admin.controller;

import com.student.ems.admin.model.DashboardStats;
import com.student.ems.admin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        DashboardStats stats = dashboardService.getStats();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        return ResponseEntity.ok(response);
    }
}
