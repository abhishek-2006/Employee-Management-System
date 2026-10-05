package com.student.ems.admin.repository;

import com.student.ems.admin.model.LeaveRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            lr.setEmployeeCode(rs.getString("employee_code"));
            lr.setEmployeeName(rs.getString("full_name"));
            lr.setDepartment(rs.getString("department"));
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

    public List<LeaveRequest> findAll(String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT lr.id, lr.employee_id, e.employee_code, e.full_name, e.department, " +
            "lr.leave_type, lr.from_date, lr.to_date, lr.reason, lr.status, lr.admin_note, " +
            "lr.applied_at, lr.reviewed_at " +
            "FROM leave_requests lr " +
            "JOIN employees e ON lr.employee_id = e.id "
        );

        List<Object> params = new ArrayList<>();
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("WHERE lr.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY CASE WHEN lr.status = 'PENDING' THEN 1 ELSE 2 END, lr.applied_at DESC");

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<LeaveRequest> findById(Integer id) {
        String sql = "SELECT lr.id, lr.employee_id, e.employee_code, e.full_name, e.department, " +
                     "lr.leave_type, lr.from_date, lr.to_date, lr.reason, lr.status, lr.admin_note, " +
                     "lr.applied_at, lr.reviewed_at " +
                     "FROM leave_requests lr " +
                     "JOIN employees e ON lr.employee_id = e.id " +
                     "WHERE lr.id = ?";
        try {
            LeaveRequest lr = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(lr);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int updateStatus(Integer id, String status, String adminNote) {
        String sql = "UPDATE leave_requests SET status = ?, admin_note = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, status, adminNote, id);
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public List<LeaveRequest> findByEmployeeId(Integer employeeId) {
        String sql = "SELECT lr.id, lr.employee_id, e.employee_code, e.full_name, e.department, " +
                     "lr.leave_type, lr.from_date, lr.to_date, lr.reason, lr.status, lr.admin_note, " +
                     "lr.applied_at, lr.reviewed_at " +
                     "FROM leave_requests lr " +
                     "JOIN employees e ON lr.employee_id = e.id " +
                     "WHERE lr.employee_id = ? " +
                     "ORDER BY lr.applied_at DESC";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }
}
