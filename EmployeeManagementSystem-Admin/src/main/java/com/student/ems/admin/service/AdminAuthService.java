package com.student.ems.admin.service;

import com.student.ems.admin.model.Admin;
import com.student.ems.admin.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Admin> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return Optional.empty();
        }

        Optional<Admin> adminOpt = adminRepository.findByUsername(username.trim());
        if (adminOpt.isEmpty()) {
            return Optional.empty();
        }

        Admin admin = adminOpt.get();
        String storedPassword = admin.getPassword();

        // Check BCrypt password match or fallback plain match for demo setup
        boolean matches = false;
        try {
            if (passwordEncoder.matches(rawPassword, storedPassword)) {
                matches = true;
            }
        } catch (Exception ignored) {
            // Ignore format exceptions
        }

        if (!matches && rawPassword.equals(storedPassword)) {
            matches = true;
        }

        if (matches) {
            // Never expose password hash outside auth service
            admin.setPassword(null);
            return Optional.of(admin);
        }

        return Optional.empty();
    }

    public Optional<Admin> getAdminById(Integer id) {
        Optional<Admin> adminOpt = adminRepository.findById(id);
        adminOpt.ifPresent(admin -> admin.setPassword(null));
        return adminOpt;
    }
}
