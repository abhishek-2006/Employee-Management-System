package com.student.ems.admin.controller;

import com.student.ems.admin.model.Attendance;
import com.student.ems.admin.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    public AdminAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAttendance(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        List<Attendance> list = attendanceService.getFilteredAttendance(employeeId, date, department, search, status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("attendance", list);
        response.put("count", list.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAttendanceById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Attendance> attOpt = attendanceService.getAttendanceById(id);
        if (attOpt.isPresent()) {
            response.put("success", true);
            response.put("attendance", attOpt.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Attendance record not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    public static class AttendanceUpdateRequest {
        private String punchIn;
        private String punchOut;
        private String status;
        public String getPunchIn() { return punchIn; }
        public void setPunchIn(String punchIn) { this.punchIn = punchIn; }
        public String getPunchOut() { return punchOut; }
        public void setPunchOut(String punchOut) { this.punchOut = punchOut; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAttendance(@PathVariable Integer id, @RequestBody AttendanceUpdateRequest req) {
        Map<String, Object> response = new HashMap<>();
        boolean updated = attendanceService.updateAttendance(id, 
                req != null ? req.getPunchIn() : null, 
                req != null ? req.getPunchOut() : null, 
                req != null ? req.getStatus() : null);
        if (updated) {
            response.put("success", true);
            response.put("message", "Attendance updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Attendance record not found or could not be updated");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
