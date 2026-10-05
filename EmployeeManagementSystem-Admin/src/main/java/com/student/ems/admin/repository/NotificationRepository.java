package com.student.ems.admin.repository;

import com.student.ems.admin.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int create(Notification notification) {
        String sql = "INSERT INTO notifications (employee_id, title, message, channel, is_read) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
            notification.getEmployeeId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getChannel() != null ? notification.getChannel() : "IN_APP",
            notification.getIsRead() != null ? notification.getIsRead() : false
        );
    }
}
