package com.student.ems.admin.service;

import com.student.ems.admin.model.Attendance;
import com.student.ems.admin.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public List<Attendance> getFilteredAttendance(Integer employeeId, String dateStr, String department, String search, String status) {
        Date workDate = null;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                workDate = Date.valueOf(dateStr.trim());
            } catch (IllegalArgumentException ignored) {}
        }
        return attendanceRepository.findFiltered(employeeId, workDate, department, search, status);
    }

    public List<Attendance> getFilteredAttendance(Integer employeeId, String dateStr, String department) {
        return getFilteredAttendance(employeeId, dateStr, department, null, null);
    }

    public Optional<Attendance> getAttendanceById(Integer id) {
        return attendanceRepository.findById(id);
    }

    public boolean updateAttendance(Integer id, String punchInStr, String punchOutStr, String status) {
        java.sql.Time punchIn = null;
        java.sql.Time punchOut = null;
        if (punchInStr != null && !punchInStr.trim().isEmpty()) {
            try {
                if (punchInStr.length() == 5) punchInStr = punchInStr + ":00";
                punchIn = java.sql.Time.valueOf(punchInStr.trim());
            } catch (Exception ignored) {}
        }
        if (punchOutStr != null && !punchOutStr.trim().isEmpty()) {
            try {
                if (punchOutStr.length() == 5) punchOutStr = punchOutStr + ":00";
                punchOut = java.sql.Time.valueOf(punchOutStr.trim());
            } catch (Exception ignored) {}
        }
        return attendanceRepository.update(id, punchIn, punchOut, status) > 0;
    }
}
