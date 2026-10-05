package com.student.ems.admin.service;

import com.student.ems.admin.model.Employee;
import com.student.ems.admin.model.LeaveRequest;
import com.student.ems.admin.model.Notification;
import com.student.ems.admin.repository.EmployeeRepository;
import com.student.ems.admin.repository.LeaveRepository;
import com.student.ems.admin.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    public LeaveService(LeaveRepository leaveRepository,
                        NotificationRepository notificationRepository,
                        EmployeeRepository employeeRepository,
                        EmailService emailService) {
        this.leaveRepository = leaveRepository;
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    public List<LeaveRequest> getAllRequests(String status) {
        return leaveRepository.findAll(status);
    }

    @Transactional
    public boolean reviewRequest(Integer leaveId, String decision, String adminNote) {
        String cleanDecision = decision != null ? decision.trim().toUpperCase() : "";
        if (!"APPROVED".equals(cleanDecision) && !"REJECTED".equals(cleanDecision)) {
            throw new IllegalArgumentException("Invalid decision: " + decision + ". Must be APPROVED or REJECTED.");
        }

        Optional<LeaveRequest> requestOpt = leaveRepository.findById(leaveId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Leave request not found with ID: " + leaveId);
        }

        LeaveRequest lr = requestOpt.get();

        // 1. Update status and note in MySQL
        int rows = leaveRepository.updateStatus(leaveId, cleanDecision, adminNote != null ? adminNote.trim() : null);
        if (rows <= 0) {
            return false;
        }

        // 2. Create in-app notification for the employee
        String title = "Leave Request Update";
        StringBuilder notifMsg = new StringBuilder();
        notifMsg.append("Your leave request from ")
                .append(lr.getFromDate())
                .append(" to ")
                .append(lr.getToDate())
                .append(" has been ")
                .append(cleanDecision)
                .append(".");
        if (adminNote != null && !adminNote.trim().isEmpty()) {
            notifMsg.append(" Note: ").append(adminNote.trim());
        }

        Notification notif = new Notification(lr.getEmployeeId(), title, notifMsg.toString(), "IN_APP");
        notificationRepository.create(notif);

        // 3. Send email if employee email is present
        Optional<Employee> empOpt = employeeRepository.findById(lr.getEmployeeId());
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            emailService.sendLeaveDecisionEmail(
                emp.getEmail(),
                emp.getFullName(),
                lr.getLeaveType(),
                String.valueOf(lr.getFromDate()),
                String.valueOf(lr.getToDate()),
                cleanDecision,
                adminNote
            );
        }

        return true;
    }
}
