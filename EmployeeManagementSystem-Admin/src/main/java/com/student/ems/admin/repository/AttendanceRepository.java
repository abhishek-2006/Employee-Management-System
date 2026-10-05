package com.student.ems.admin.repository;

import com.student.ems.admin.model.Attendance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Attendance> rowMapper = new RowMapper<>() {
        @Override
        public Attendance mapRow(ResultSet rs, int rowNum) throws SQLException {
            Attendance a = new Attendance();
            a.setId(rs.getInt("id"));
            a.setEmployeeId(rs.getInt("employee_id"));
            a.setEmployeeCode(rs.getString("employee_code"));
            a.setEmployeeName(rs.getString("full_name"));
            a.setDepartment(rs.getString("department"));
            a.setWorkDate(rs.getDate("work_date"));
            a.setPunchIn(rs.getTime("punch_in"));
            a.setPunchOut(rs.getTime("punch_out"));
            a.setWorkMinutes(rs.getInt("work_minutes"));
            a.setPunchMethod(rs.getString("punch_method"));
            try {
                a.setPhoto(rs.getString("photo"));
            } catch (SQLException ignored) {}
            try {
                a.setPunchInPhoto(rs.getString("punch_in_photo"));
            } catch (SQLException ignored) {}
            try {
                a.setPunchOutPhoto(rs.getString("punch_out_photo"));
            } catch (SQLException ignored) {}
            try {
                a.setStatus(rs.getString("status"));
            } catch (SQLException ignored) {}
            return a;
        }
    };

    public List<Attendance> findFiltered(Integer employeeId, Date workDate, String department, String search, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.id, a.employee_id, e.employee_code, e.full_name, e.department, e.photo, " +
            "a.work_date, a.punch_in, a.punch_out, a.work_minutes, a.punch_method, " +
            "a.punch_in_photo, a.punch_out_photo, a.status " +
            "FROM attendance a " +
            "JOIN employees e ON a.employee_id = e.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (employeeId != null && employeeId > 0) {
            sql.append("AND a.employee_id = ? ");
            params.add(employeeId);
        }

        if (workDate != null) {
            sql.append("AND a.work_date = ? ");
            params.add(workDate);
        }

        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
            sql.append("AND e.department = ? ");
            params.add(department.trim());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND a.status = ? ");
            params.add(status.trim());
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(e.full_name) LIKE ? OR LOWER(e.employee_code) LIKE ?) ");
            String q = "%" + search.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
        }

        sql.append("ORDER BY a.work_date DESC, a.punch_in DESC");

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Attendance> findFiltered(Integer employeeId, Date workDate, String department) {
        return findFiltered(employeeId, workDate, department, null, null);
    }

    public Optional<Attendance> findById(Integer id) {
        String sql = "SELECT a.id, a.employee_id, e.employee_code, e.full_name, e.department, e.photo, " +
                     "a.work_date, a.punch_in, a.punch_out, a.work_minutes, a.punch_method, " +
                     "a.punch_in_photo, a.punch_out_photo, a.status " +
                     "FROM attendance a " +
                     "JOIN employees e ON a.employee_id = e.id " +
                     "WHERE a.id = ?";
        try {
            Attendance a = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(a);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int update(Integer id, java.sql.Time punchIn, java.sql.Time punchOut, String status) {
        int workMinutes = 0;
        if (punchIn != null && punchOut != null) {
            workMinutes = (int) Math.max(0, java.time.Duration.between(punchIn.toLocalTime(), punchOut.toLocalTime()).toMinutes());
        }
        String sql = "UPDATE attendance SET punch_in = ?, punch_out = ?, work_minutes = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, punchIn, punchOut, workMinutes, status != null ? status : "PRESENT", id);
    }

    public int countTodayPresent() {
        String sql = "SELECT COUNT(*) FROM attendance WHERE work_date = CURRENT_DATE() AND punch_in IS NOT NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countTodayPunchOut() {
        String sql = "SELECT COUNT(*) FROM attendance WHERE work_date = CURRENT_DATE() AND punch_out IS NOT NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
