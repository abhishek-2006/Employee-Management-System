package com.student.ems.employee.controller;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.model.FaceEnrollRequest;
import com.student.ems.employee.model.FacePunchRequest;
import com.student.ems.employee.service.FaceAttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/employee/face")
public class EmployeeFaceController {

    private final FaceAttendanceService faceAttendanceService;

    public EmployeeFaceController(FaceAttendanceService faceAttendanceService) {
        this.faceAttendanceService = faceAttendanceService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enrollFace(@RequestBody FaceEnrollRequest enrollRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        Map<String, Object> result = faceAttendanceService.enrollFace(emp.getId(), enrollRequest != null ? enrollRequest.getDescriptor() : null);
        if (Boolean.TRUE.equals(result.get("success"))) {
            emp.setFaceEnrolled(true);
            session.setAttribute("EMPLOYEE_USER", emp);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/punch")
    public ResponseEntity<Map<String, Object>> punchAttendance(@RequestBody FacePunchRequest punchRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee emp = (Employee) session.getAttribute("EMPLOYEE_USER");

        Map<String, Object> result = faceAttendanceService.punchAttendance(emp.getId(), punchRequest != null ? punchRequest.getDescriptor() : null);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
