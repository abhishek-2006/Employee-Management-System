package com.student.ems.employee.repository;

import com.student.ems.employee.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> rowMapper = new RowMapper<>() {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setEmployeeId(rs.getInt("employee_id"));
            n.setTitle(rs.getString("title"));
            n.setMessage(rs.getString("message"));
            n.setChannel(rs.getString("channel"));
            n.setIsRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getTimestamp("created_at"));
            return n;
        }
    };

    public List<Notification> findByEmployee(Integer employeeId) {
        String sql = "SELECT id, employee_id, title, message, channel, is_read, created_at " +
                     "FROM notifications WHERE employee_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }

    public int markAsRead(Integer id, Integer employeeId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND employee_id = ?";
        return jdbcTemplate.update(sql, id, employeeId);
    }

    public int markAllAsRead(Integer employeeId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE employee_id = ?";
        return jdbcTemplate.update(sql, employeeId);
    }

    public int countUnread(Integer employeeId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE employee_id = ? AND is_read = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeId);
        return count != null ? count : 0;
    }
}
