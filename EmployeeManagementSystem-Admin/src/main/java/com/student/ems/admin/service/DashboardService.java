package com.student.ems.admin.service;

import com.student.ems.admin.model.DashboardStats;
import com.student.ems.admin.repository.AttendanceRepository;
import com.student.ems.admin.repository.EmployeeRepository;
import com.student.ems.admin.repository.LeaveRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveRepository leaveRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
    }

    public DashboardStats getStats() {
        int total = employeeRepository.countTotal();
        int active = employeeRepository.countActive();
        int inactive = employeeRepository.countInactive();
        int departments = employeeRepository.countDepartments();
        int presentToday = attendanceRepository.countTodayPresent();
        int punchOutToday = attendanceRepository.countTodayPunchOut();
        int pendingLeaves = leaveRepository.countPending();

        return new DashboardStats(total, active, inactive, departments, presentToday, punchOutToday, pendingLeaves);
    }
}
