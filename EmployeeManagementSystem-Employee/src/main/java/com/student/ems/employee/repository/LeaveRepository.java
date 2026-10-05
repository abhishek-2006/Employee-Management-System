package com.student.ems.employee.repository;

import com.student.ems.employee.model.LeaveRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class LeaveRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeaveRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<LeaveRequest> rowMapper = new RowMapper<>() {
        @Override
        public LeaveRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeaveRequest lr = new LeaveRequest();
            lr.setId(rs.getInt("id"));
            lr.setEmployeeId(rs.getInt("employee_id"));
            lr.setLeaveType(rs.getString("leave_type"));
            lr.setFromDate(rs.getDate("from_date"));
            lr.setToDate(rs.getDate("to_date"));
            lr.setReason(rs.getString("reason"));
            lr.setStatus(rs.getString("status"));
            lr.setAdminNote(rs.getString("admin_note"));
            lr.setAppliedAt(rs.getTimestamp("applied_at"));
            lr.setReviewedAt(rs.getTimestamp("reviewed_at"));
            return lr;
        }
    };

    public int create(LeaveRequest lr) {
        String sql = "INSERT INTO leave_requests (employee_id, leave_type, from_date, to_date, reason, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        return jdbcTemplate.update(sql,
            lr.getEmployeeId(),
            lr.getLeaveType(),
            lr.getFromDate(),
            lr.getToDate(),
            lr.getReason()
        );
    }

    public List<LeaveRequest> findByEmployee(Integer employeeId) {
        String sql = "SELECT id, employee_id, leave_type, from_date, to_date, reason, status, admin_note, " +
                     "applied_at, reviewed_at FROM leave_requests WHERE employee_id = ? ORDER BY applied_at DESC";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }
}
