package com.student.ems.employee.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.ems.employee.model.Attendance;
import com.student.ems.employee.model.Employee;
import com.student.ems.employee.repository.AttendanceRepository;
import com.student.ems.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FaceAttendanceService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ObjectMapper objectMapper;

    // Standard face-api Euclidean distance matching threshold
    private static final double MATCH_THRESHOLD = 0.60;

    public FaceAttendanceService(EmployeeRepository employeeRepository,
                                 AttendanceRepository attendanceRepository,
                                 ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> enrollFace(Integer employeeId, List<Double> descriptor) {
        Map<String, Object> result = new HashMap<>();

        if (descriptor == null || descriptor.size() < 64) {
            result.put("success", false);
            result.put("message", "Please keep your face inside the camera.");
            return result;
        }

        try {
            String jsonDescriptor = objectMapper.writeValueAsString(descriptor);
            int updated = employeeRepository.updateFaceDescriptor(employeeId, jsonDescriptor);
            if (updated > 0) {
                result.put("success", true);
                result.put("message", "Face enrolled successfully.");
            } else {
                result.put("success", false);
                result.put("message", "Employee record not found for enrollment.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to store face descriptor: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> punchAttendance(Integer employeeId, List<Double> currentDescriptor) {
        Map<String, Object> result = new HashMap<>();

        if (currentDescriptor == null || currentDescriptor.size() < 64) {
            result.put("success", false);
            result.put("message", "Please keep your face inside the camera.");
            return result;
        }

        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Employee not found.");
            return result;
        }

        Employee emp = empOpt.get();
        String enrolledJson = emp.getFaceDescriptor();

        if (enrolledJson == null || enrolledJson.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Please enroll your face first before punching attendance.");
            return result;
        }

        List<Double> enrolledDescriptor;
        try {
            enrolledDescriptor = objectMapper.readValue(enrolledJson, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error reading stored face descriptor. Please re-enroll your face.");
            return result;
        }

        if (enrolledDescriptor.size() != currentDescriptor.size()) {
            result.put("success", false);
            result.put("message", "Face descriptor dimension mismatch. Please re-enroll.");
            return result;
        }

        // Compare using Euclidean distance
        double distance = calculateEuclideanDistance(currentDescriptor, enrolledDescriptor);
        if (distance > MATCH_THRESHOLD) {
            result.put("success", false);
            result.put("message", "Face does not match the enrolled employee face.");
            result.put("distance", Math.round(distance * 100.0) / 100.0);
            return result;
        }

        // Face matches! Process Punch In / Punch Out
        LocalTime now = LocalTime.now();
        Time currentTime = Time.valueOf(now);
        Optional<Attendance> todayAttOpt = attendanceRepository.findTodayAttendance(employeeId);

        if (todayAttOpt.isEmpty()) {
            // Case 1: No punch in today -> Record Punch In
            attendanceRepository.insertPunchIn(employeeId, currentTime, "FACE");
            result.put("success", true);
            result.put("action", "PUNCH_IN");
            result.put("message", "Punch In successful.");
            result.put("punchTime", currentTime.toString());
            return result;
        }

        Attendance todayAtt = todayAttOpt.get();
        if (todayAtt.getPunchOut() == null) {
            // Case 2: Punch In exists and Punch Out does not exist -> Record Punch Out
            LocalTime inTime = todayAtt.getPunchIn().toLocalTime();
            int workMinutes = (int) Math.max(0, Duration.between(inTime, now).toMinutes());

            attendanceRepository.updatePunchOut(todayAtt.getId(), currentTime, workMinutes);
            result.put("success", true);
            result.put("action", "PUNCH_OUT");
            result.put("message", "Punch Out successful.");
            result.put("punchTime", currentTime.toString());
            result.put("workMinutes", workMinutes);
            int h = workMinutes / 60;
            int m = workMinutes % 60;
            result.put("workHours", h + "h " + m + "m");
            return result;
        }

        // Case 3: Both Punch In and Punch Out are already completed
        result.put("success", false);
        result.put("message", "Today's Punch In and Punch Out are already completed.");
        return result;
    }

    private double calculateEuclideanDistance(List<Double> a, List<Double> b) {
        double sum = 0.0;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            double diff = a.get(i) - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
