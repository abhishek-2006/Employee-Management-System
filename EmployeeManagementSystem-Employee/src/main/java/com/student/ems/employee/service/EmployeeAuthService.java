package com.student.ems.employee.service;

import com.student.ems.employee.model.Employee;
import com.student.ems.employee.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeAuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAuthService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Employee> authenticate(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null) {
            return Optional.empty();
        }

        String search = identifier.trim();
        Optional<Employee> empOpt = employeeRepository.findByUsername(search);
        if (empOpt.isEmpty()) {
            empOpt = employeeRepository.findByEmployeeCode(search.toUpperCase());
        }
        if (empOpt.isEmpty()) {
            empOpt = employeeRepository.findByEmail(search.toLowerCase());
        }
        if (empOpt.isEmpty()) {
            return Optional.empty();
        }

        Employee emp = empOpt.get();

        // Check if account is active
        if (!"ACTIVE".equalsIgnoreCase(emp.getStatus())) {
            throw new IllegalStateException("Your account is currently inactive. Please contact the administrator.");
        }

        String storedPassword = emp.getPassword();
        boolean matches = false;
        try {
            if (passwordEncoder.matches(rawPassword, storedPassword)) {
                matches = true;
            }
        } catch (Exception ignored) {}

        if (!matches && rawPassword.equals(storedPassword)) {
            matches = true;
        }

        if (matches) {
            emp.setPassword(null);
            return Optional.of(emp);
        }

        return Optional.empty();
    }

    public Optional<Employee> getEmployeeById(Integer id) {
        Optional<Employee> emp = employeeRepository.findById(id);
        emp.ifPresent(e -> e.setPassword(null));
        return emp;
    }

    public boolean updateProfile(Integer id, String phone, String address, String gender, java.sql.Date dob) {
        Optional<Employee> empOpt = employeeRepository.findById(id);
        if (empOpt.isEmpty()) return false;
        Employee emp = empOpt.get();
        if (phone != null) emp.setPhone(phone.trim());
        if (address != null) emp.setAddress(address.trim());
        if (gender != null) emp.setGender(gender.trim());
        if (dob != null) emp.setDob(dob);
        return employeeRepository.updateProfile(emp) > 0;
    }

    public boolean updatePhoto(Integer id, String photo) {
        return employeeRepository.updatePhoto(id, photo) > 0;
    }

    public boolean changePassword(Integer id, String oldPassword, String newPassword) {
        Optional<Employee> empOpt = employeeRepository.findById(id);
        if (empOpt.isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        Employee emp = empOpt.get();
        if (oldPassword != null && (passwordEncoder.matches(oldPassword, emp.getPassword()) || oldPassword.equals(emp.getPassword()))) {
            String encoded = passwordEncoder.encode(newPassword.trim());
            return employeeRepository.updatePassword(id, encoded) > 0;
        }
        return false;
    }
}
