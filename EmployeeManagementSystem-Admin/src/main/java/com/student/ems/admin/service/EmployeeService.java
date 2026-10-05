package com.student.ems.admin.service;

import com.student.ems.admin.model.Attendance;
import com.student.ems.admin.model.Employee;
import com.student.ems.admin.model.LeaveRequest;
import com.student.ems.admin.repository.AttendanceRepository;
import com.student.ems.admin.repository.EmployeeRepository;
import com.student.ems.admin.repository.LeaveRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           AttendanceRepository attendanceRepository,
                           LeaveRepository leaveRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    public Map<String, Object> getEmployeeFullDetails(Integer id) {
        Optional<Employee> empOpt = employeeRepository.findById(id);
        if (empOpt.isEmpty()) {
            return null;
        }

        Employee emp = empOpt.get();
        List<Attendance> attendances = attendanceRepository.findFiltered(id, null, null);
        List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(id);

        Map<String, Object> details = new HashMap<>();
        details.put("employee", emp);
        details.put("attendances", attendances);
        details.put("leaves", leaves);
        details.put("totalAttendanceCount", attendances.size());
        details.put("totalLeaveCount", leaves.size());

        long pendingLeaves = leaves.stream().filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();
        details.put("pendingLeaveCount", pendingLeaves);

        return details;
    }

    public Employee createEmployee(Employee employee) {
        // Validation
        if (employee.getEmployeeCode() == null || employee.getEmployeeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee Code is required");
        }
        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required");
        }
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (employee.getPassword() == null || employee.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Initial Password is required");
        }

        String empCode = employee.getEmployeeCode().trim().toUpperCase();
        if (employeeRepository.findByEmployeeCode(empCode).isPresent()) {
            throw new IllegalArgumentException("Employee Code '" + empCode + "' already exists");
        }

        String username = employee.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = empCode;
        }
        username = username.trim();
        if (employeeRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' already exists");
        }
        employee.setUsername(username);

        String email = employee.getEmail().trim().toLowerCase();
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' already exists");
        }

        // Hash initial password using BCrypt
        employee.setEmployeeCode(empCode);
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(employee.getPassword().trim()));
        if (employee.getStatus() == null || employee.getStatus().trim().isEmpty()) {
            employee.setStatus("ACTIVE");
        }
        if (employee.getGender() == null || employee.getGender().trim().isEmpty()) {
            employee.setGender("Not Specified");
        }
        if (employee.getSalary() == null) {
            employee.setSalary(0.0);
        }

        Integer generatedId = employeeRepository.save(employee);
        employee.setId(generatedId);
        employee.setPassword(null);
        return employee;
    }

    public boolean resetPassword(Integer id, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters long");
        }
        Optional<Employee> existing = employeeRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        String encoded = passwordEncoder.encode(newPassword.trim());
        return employeeRepository.resetPassword(id, encoded) > 0;
    }

    public Employee updateEmployee(Integer id, Employee employee) {
        Optional<Employee> existingOpt = employeeRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }

        Employee existing = existingOpt.get();
        existing.setFullName(employee.getFullName().trim());
        existing.setEmail(employee.getEmail().trim().toLowerCase());
        existing.setPhone(employee.getPhone() != null ? employee.getPhone().trim() : null);
        existing.setDepartment(employee.getDepartment() != null ? employee.getDepartment().trim() : null);
        existing.setDesignation(employee.getDesignation() != null ? employee.getDesignation().trim() : null);
        existing.setAddress(employee.getAddress() != null ? employee.getAddress().trim() : null);
        existing.setJoinDate(employee.getJoinDate());
        if (employee.getGender() != null) {
            existing.setGender(employee.getGender());
        }
        if (employee.getDob() != null) {
            existing.setDob(employee.getDob());
        }
        if (employee.getSalary() != null) {
            existing.setSalary(employee.getSalary());
        }
        if (employee.getPhoto() != null && !employee.getPhoto().trim().isEmpty()) {
            existing.setPhoto(employee.getPhoto());
        }

        employeeRepository.update(existing);
        existing.setPassword(null);
        return existing;
    }

    public boolean updatePhoto(Integer id, String photo) {
        return employeeRepository.updatePhoto(id, photo) > 0;
    }

    public boolean deleteEmployee(Integer id) {
        return employeeRepository.delete(id) > 0;
    }

    public boolean updateStatus(Integer id, String status) {
        String cleanStatus = status != null ? status.trim().toUpperCase() : "ACTIVE";
        if (!"ACTIVE".equals(cleanStatus) && !"INACTIVE".equals(cleanStatus)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: ACTIVE, INACTIVE");
        }
        return employeeRepository.updateStatus(id, cleanStatus) > 0;
    }
}
