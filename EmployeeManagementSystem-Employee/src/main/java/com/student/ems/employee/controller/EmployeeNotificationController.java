package com.student.ems.employee.controller;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.model.Notification;
import com.student.ems.employee.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/notifications")
public class EmployeeNotificationController {

    private final NotificationRepository notificationRepository;

    public EmployeeNotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        List<Notification> list = notificationRepository.findByEmployee(emp.getId());
        int unread = notificationRepository.countUnread(emp.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("notifications", list);
        response.put("unreadCount", unread);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Integer id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        int updated = notificationRepository.markAsRead(id, emp.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", updated > 0);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        notificationRepository.markAllAsRead(emp.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }
}
