package com.student.ems.admin.repository;

import com.student.ems.admin.model.Employee;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
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
            try {
                emp.setUsername(rs.getString("username"));
            } catch (SQLException ignored) {}
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

    public List<Employee> findAll() {
        String sql = BASE_SELECT + "ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper);
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

    public Optional<Employee> findByEmployeeCode(String employeeCode) {
        String sql = BASE_SELECT + "WHERE employee_code = ?";
        try {
            Employee emp = jdbcTemplate.queryForObject(sql, rowMapper, employeeCode);
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

    public Integer save(Employee employee) {
        String sql = "INSERT INTO employees (employee_code, username, full_name, email, phone, department, " +
                     "designation, address, join_date, password, status, gender, dob, salary, photo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, employee.getEmployeeCode());
            String u = employee.getUsername();
            if (u == null || u.trim().isEmpty()) {
                u = employee.getEmployeeCode();
            }
            ps.setString(2, u.trim());
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getEmail());
            ps.setString(5, employee.getPhone());
            ps.setString(6, employee.getDepartment());
            ps.setString(7, employee.getDesignation());
            ps.setString(8, employee.getAddress());
            ps.setDate(9, employee.getJoinDate());
            ps.setString(10, employee.getPassword());
            ps.setString(11, employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
            ps.setString(12, employee.getGender() != null ? employee.getGender() : "Not Specified");
            ps.setDate(13, employee.getDob());
            ps.setObject(14, employee.getSalary() != null ? employee.getSalary() : 0.0);
            ps.setString(15, employee.getPhoto());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    public int update(Employee employee) {
        String sql = "UPDATE employees SET username = COALESCE(?, username), full_name = ?, email = ?, phone = ?, department = ?, " +
                     "designation = ?, address = ?, join_date = ?, gender = ?, dob = ?, salary = ?, " +
                     "photo = COALESCE(?, photo) WHERE id = ?";
        return jdbcTemplate.update(sql,
            employee.getUsername(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getPhone(),
            employee.getDepartment(),
            employee.getDesignation(),
            employee.getAddress(),
            employee.getJoinDate(),
            employee.getGender() != null ? employee.getGender() : "Not Specified",
            employee.getDob(),
            employee.getSalary() != null ? employee.getSalary() : 0.0,
            employee.getPhoto(),
            employee.getId()
        );
    }

    public int resetPassword(Integer id, String encodedPassword) {
        String sql = "UPDATE employees SET password = ? WHERE id = ?";
        return jdbcTemplate.update(sql, encodedPassword, id);
    }

    public int updatePhoto(Integer id, String photo) {
        String sql = "UPDATE employees SET photo = ? WHERE id = ?";
        return jdbcTemplate.update(sql, photo, id);
    }

    public int delete(Integer id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int updateStatus(Integer id, String status) {
        String sql = "UPDATE employees SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM employees";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM employees WHERE status = 'ACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countInactive() {
        String sql = "SELECT COUNT(*) FROM employees WHERE status = 'INACTIVE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countDepartments() {
        String sql = "SELECT COUNT(DISTINCT department) FROM employees WHERE department IS NOT NULL AND TRIM(department) != ''";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
