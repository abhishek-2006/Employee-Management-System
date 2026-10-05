package com.student.ems.employee.controller;

import com.student.ems.employee.model.Attendance;
import com.student.ems.employee.model.Employee;
import com.student.ems.employee.repository.AttendanceRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee/attendance")
public class EmployeeAttendanceController {

    private final AttendanceRepository attendanceRepository;

    public EmployeeAttendanceController(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAttendanceStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        Optional<Attendance> todayOpt = attendanceRepository.findTodayAttendance(emp.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        if (todayOpt.isPresent()) {
            Attendance att = todayOpt.get();
            response.put("punchedIn", att.getPunchIn() != null);
            response.put("punchedOut", att.getPunchOut() != null);
            response.put("punchInTime", att.getPunchIn() != null ? att.getPunchIn().toString() : null);
            response.put("punchOutTime", att.getPunchOut() != null ? att.getPunchOut().toString() : null);
            response.put("punchInPhoto", att.getPunchInPhoto());
            response.put("punchOutPhoto", att.getPunchOutPhoto());
            response.put("workMinutes", att.getWorkMinutes());
            response.put("formattedWorkHours", att.getFormattedWorkHours());
            response.put("status", att.getStatus());
        } else {
            response.put("punchedIn", false);
            response.put("punchedOut", false);
            response.put("punchInTime", null);
            response.put("punchOutTime", null);
            response.put("punchInPhoto", null);
            response.put("punchOutPhoto", null);
            response.put("workMinutes", 0);
            response.put("formattedWorkHours", "0h 0m");
            response.put("status", "NOT_MARKED");
        }

        return ResponseEntity.ok(response);
    }

    public static class PunchRequest {
        private String photo;
        public String getPhoto() { return photo; }
        public void setPhoto(String photo) { this.photo = photo; }
    }

    @org.springframework.web.bind.annotation.PostMapping("/punch-in")
    public ResponseEntity<Map<String, Object>> punchIn(@org.springframework.web.bind.annotation.RequestBody(required = false) PunchRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        Optional<Attendance> todayOpt = attendanceRepository.findTodayAttendance(emp.getId());
        if (todayOpt.isPresent() && todayOpt.get().getPunchIn() != null) {
            response.put("success", false);
            response.put("message", "You have already punched in today.");
            return ResponseEntity.badRequest().body(response);
        }

        String photo = (req != null && req.getPhoto() != null && !req.getPhoto().trim().isEmpty()) ? req.getPhoto() : null;
        java.time.LocalTime now = java.time.LocalTime.now();
        java.sql.Time time = java.sql.Time.valueOf(now);
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.format.DateTimeFormatter ttf = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");

        attendanceRepository.insertPunchInWithPhoto(emp.getId(), time, "CAMERA", photo);

        response.put("success", true);
        response.put("message", "Punch In successful");
        response.put("date", today.format(dtf));
        response.put("time", now.format(ttf));
        response.put("rawTime", time.toString());
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.PostMapping("/punch-out")
    public ResponseEntity<Map<String, Object>> punchOut(@org.springframework.web.bind.annotation.RequestBody(required = false) PunchRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");
        Map<String, Object> response = new HashMap<>();

        Optional<Attendance> todayOpt = attendanceRepository.findTodayAttendance(emp.getId());
        if (todayOpt.isEmpty() || todayOpt.get().getPunchIn() == null) {
            response.put("success", false);
            response.put("message", "Please punch in before punching out.");
            return ResponseEntity.badRequest().body(response);
        }

        Attendance todayAtt = todayOpt.get();
        if (todayAtt.getPunchOut() != null) {
            response.put("success", false);
            response.put("message", "You have already completed today's attendance.");
            return ResponseEntity.badRequest().body(response);
        }

        String photo = (req != null && req.getPhoto() != null && !req.getPhoto().trim().isEmpty()) ? req.getPhoto() : null;
        java.time.LocalTime now = java.time.LocalTime.now();
        java.sql.Time outTime = java.sql.Time.valueOf(now);
        java.time.LocalTime inTime = todayAtt.getPunchIn().toLocalTime();
        int workMinutes = (int) Math.max(0, java.time.Duration.between(inTime, now).toMinutes());

        attendanceRepository.updatePunchOutWithPhoto(todayAtt.getId(), outTime, workMinutes, photo);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        java.time.format.DateTimeFormatter ttf = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
        int h = workMinutes / 60;
        int m = workMinutes % 60;

        response.put("success", true);
        response.put("message", "Punch Out successful");
        response.put("date", today.format(dtf));
        response.put("time", now.format(ttf));
        response.put("rawTime", outTime.toString());
        response.put("workHours", h + "h " + m + "m");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAttendanceHistory(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        List<Attendance> history = attendanceRepository.findHistory(emp.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("history", history);
        return ResponseEntity.ok(response);
    }
}
