package com.student.ems.employee.repository;

import com.student.ems.employee.model.Attendance;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
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
            a.setWorkDate(rs.getDate("work_date"));
            a.setPunchIn(rs.getTime("punch_in"));
            a.setPunchOut(rs.getTime("punch_out"));
            a.setWorkMinutes(rs.getInt("work_minutes"));
            a.setPunchMethod(rs.getString("punch_method"));
            a.setPunchInPhoto(rs.getString("punch_in_photo"));
            a.setPunchOutPhoto(rs.getString("punch_out_photo"));
            a.setStatus(rs.getString("status"));
            return a;
        }
    };

    public Optional<Attendance> findTodayAttendance(Integer employeeId) {
        String sql = "SELECT id, employee_id, work_date, punch_in, punch_out, work_minutes, punch_method, punch_in_photo, punch_out_photo, status " +
                     "FROM attendance WHERE employee_id = ? AND work_date = CURRENT_DATE()";
        try {
            Attendance att = jdbcTemplate.queryForObject(sql, rowMapper, employeeId);
            return Optional.ofNullable(att);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int insertPunchIn(Integer employeeId, Time punchIn, String method) {
        return insertPunchInWithPhoto(employeeId, punchIn, method, null);
    }

    public int insertPunchInWithPhoto(Integer employeeId, Time punchIn, String method, String photo) {
        String sql = "INSERT INTO attendance (employee_id, work_date, punch_in, punch_method, punch_in_photo, status, work_minutes) " +
                     "VALUES (?, CURRENT_DATE(), ?, ?, ?, 'PRESENT', 0)";
        return jdbcTemplate.update(sql, employeeId, punchIn, method != null ? method : "CAMERA", photo);
    }

    public int updatePunchOut(Integer attendanceId, Time punchOut, int workMinutes) {
        return updatePunchOutWithPhoto(attendanceId, punchOut, workMinutes, null);
    }

    public int updatePunchOutWithPhoto(Integer attendanceId, Time punchOut, int workMinutes, String photo) {
        String sql = "UPDATE attendance SET punch_out = ?, punch_out_photo = ?, work_minutes = ? WHERE id = ?";
        return jdbcTemplate.update(sql, punchOut, photo, workMinutes, attendanceId);
    }

    public List<Attendance> findHistory(Integer employeeId) {
        String sql = "SELECT id, employee_id, work_date, punch_in, punch_out, work_minutes, punch_method, punch_in_photo, punch_out_photo, status " +
                     "FROM attendance WHERE employee_id = ? ORDER BY work_date DESC, punch_in DESC";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }
}
