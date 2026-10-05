package com.student.ems.employee.service;

import com.student.ems.employee.model.LeaveRequest;
import com.student.ems.employee.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public boolean applyLeave(Integer employeeId, LeaveRequest request) {
        if (request.getLeaveType() == null || request.getLeaveType().trim().isEmpty()) {
            throw new IllegalArgumentException("Leave type is required.");
        }
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new IllegalArgumentException("From date and To date are required.");
        }
        if (request.getFromDate().after(request.getToDate())) {
            throw new IllegalArgumentException("From date cannot be after To date.");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required.");
        }

        request.setEmployeeId(employeeId);
        return leaveRepository.create(request) > 0;
    }

    public List<LeaveRequest> getMyLeaves(Integer employeeId) {
        return leaveRepository.findByEmployee(employeeId);
    }
}
