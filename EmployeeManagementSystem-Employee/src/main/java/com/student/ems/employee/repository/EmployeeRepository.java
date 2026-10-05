package com.student.ems.employee.repository;

import com.student.ems.employee.model.Employee;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Employee> rowMapper = new RowMapper<>() {
        @Override
        public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
            Employee emp = new Employee();
            emp.setId(rs.getInt("id"));
            emp.setEmployeeCode(rs.getString("employee_code"));
            emp.setFullName(rs.getString("full_name"));
            emp.setEmail(rs.getString("email"));
            emp.setPhone(rs.getString("phone"));
            emp.setDepartment(rs.getString("department"));
            emp.setDesignation(rs.getString("designation"));
            emp.setAddress(rs.getString("address"));
            emp.setJoinDate(rs.getDate("join_date"));
            emp.setPassword(rs.getString("password"));
            emp.setFaceDescriptor(rs.getString("face_descriptor"));
            emp.setStatus(rs.getString("status"));
            emp.setCreatedAt(rs.getTimestamp("created_at"));

            try {
                emp.setUsername(rs.getString("username"));
            } catch (SQLException ignored) {}

            try {
                emp.setGender(rs.getString("gender"));
            } catch (SQLException ignored) {}

            try {
                emp.setDob(rs.getDate("dob"));
            } catch (SQLException ignored) {}

            try {
                emp.setSalary(rs.getObject("salary") != null ? rs.getDouble("salary") : 0.0);
            } catch (SQLException ignored) {}

            try {
                emp.setPhoto(rs.getString("photo"));
            } catch (SQLException ignored) {}

            return emp;
        }
    };

    private static final String BASE_SELECT = 
        "SELECT id, employee_code, username, full_name, email, phone, department, designation, " +
        "address, join_date, password, face_descriptor, status, created_at, " +
        "gender, dob, salary, photo FROM employees ";

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        String sql = BASE_SELECT + "WHERE employee_code = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, rowMapper, employeeCode.trim());
            return Optional.ofNullable(emp);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Employee> findByUsername(String username) {
        String sql = BASE_SELECT + "WHERE username = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, rowMapper, username.trim());
            return Optional.ofNullable(emp);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Employee> findByEmail(String email) {
        String sql = BASE_SELECT + "WHERE LOWER(email) = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, rowMapper, email.trim().toLowerCase());
            return Optional.ofNullable(emp);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Employee> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE id = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(emp);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int updateFaceDescriptor(Integer id, String descriptorJson) {
        String sql = "UPDATE employees SET face_descriptor = ? WHERE id = ?";
        return jdbcTemplate.update(sql, descriptorJson, id);
    }

    public int updateProfile(Employee emp) {
        String sql = "UPDATE employees SET phone = ?, address = ?, gender = ?, dob = ? WHERE id = ?";
        return jdbcTemplate.update(sql, emp.getPhone(), emp.getAddress(), emp.getGender(), emp.getDob(), emp.getId());
    }

    public int updatePhoto(Integer id, String photo) {
        String sql = "UPDATE employees SET photo = ? WHERE id = ?";
        return jdbcTemplate.update(sql, photo, id);
    }

    public int updatePassword(Integer id, String encodedPassword) {
        String sql = "UPDATE employees SET password = ? WHERE id = ?";
        return jdbcTemplate.update(sql, encodedPassword, id);
    }
}
