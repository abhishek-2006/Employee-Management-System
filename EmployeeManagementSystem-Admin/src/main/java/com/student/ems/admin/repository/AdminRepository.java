package com.student.ems.admin.repository;

import com.student.ems.admin.model.Admin;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Admin> rowMapper = new RowMapper<>() {
        @Override
        public Admin mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Admin(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getTimestamp("created_at")
            );
        }
    };

    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT id, username, password, full_name, created_at FROM admin WHERE username = ?";
        try {
            Admin admin = jdbcTemplate.queryForObject(sql, rowMapper, username);
            return Optional.ofNullable(admin);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Admin> findById(Integer id) {
        String sql = "SELECT id, username, password, full_name, created_at FROM admin WHERE id = ?";
        try {
            Admin admin = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(admin);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
