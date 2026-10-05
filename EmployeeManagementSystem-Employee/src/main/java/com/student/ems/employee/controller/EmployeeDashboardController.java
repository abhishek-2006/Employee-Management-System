package com.student.ems.employee.controller;

import com.student.ems.employee.model.Attendance;
import com.student.ems.employee.model.Employee;
import com.student.ems.employee.model.TodayAttendanceStatus;
import com.student.ems.employee.repository.AttendanceRepository;
import com.student.ems.employee.repository.EmployeeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employee/dashboard")
public class EmployeeDashboardController {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeDashboardController(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee sessionEmp = (Employee) session.getAttribute("EMPLOYEE_USER");

        // Fresh employee data
        Optional<Employee> empOpt = employeeRepository.findById(sessionEmp.getId());
        Employee emp = empOpt.orElse(sessionEmp);

        TodayAttendanceStatus status = new TodayAttendanceStatus();
        status.setEmployeeName(emp.getFullName());
        status.setEmployeeCode(emp.getEmployeeCode());
        status.setDepartment(emp.getDepartment());
        status.setDesignation(emp.getDesignation());
        status.setFaceEnrolled(emp.isFaceEnrolled());

        Optional<Attendance> todayAtt = attendanceRepository.findTodayAttendance(emp.getId());
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm a");

        if (todayAtt.isEmpty()) {
            status.setTodayStatus("NOT_PUNCHED");
            status.setTodayPunchIn("-");
            status.setTodayPunchOut("-");
            status.setTodayWorkHours("-");
        } else {
            Attendance a = todayAtt.get();
            status.setTodayPunchIn(a.getPunchIn() != null ? tf.format(a.getPunchIn()) : "-");

            if (a.getPunchOut() != null) {
                status.setTodayStatus("COMPLETED");
                status.setTodayPunchOut(tf.format(a.getPunchOut()));
                int wm = a.getWorkMinutes() != null ? a.getWorkMinutes() : 0;
                status.setTodayWorkHours((wm / 60) + "h " + (wm % 60) + "m");
            } else {
                status.setTodayStatus("WORKING");
                status.setTodayPunchOut("-");
                status.setTodayWorkHours("In Progress");
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", status);
        return ResponseEntity.ok(response);
    }
}
