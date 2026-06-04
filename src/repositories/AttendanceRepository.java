package src.repositories;

import src.models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {

    private final Connection con;

    public AttendanceRepository(Connection con) {
        this.con = con;
    }

    // ── upsert single record ──
    public void upsert(Attendance a) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO attendance " +
            "(student_id, course_id, attendance_date, status, remarks) " +
            "VALUES (?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE status=VALUES(status), remarks=VALUES(remarks)");
        ps.setInt(1, a.getStudentId());
        ps.setInt(2, a.getCourseId());
        ps.setString(3, a.getAttendanceDate().toString());
        ps.setString(4, a.getStatus());
        ps.setString(5, a.getRemarks() != null ? a.getRemarks() : "");
        ps.executeUpdate();
    }

    // ── get attendance for a course on a date ──
    public List<Attendance> getByCoursAndDate(int courseId,
                                               String date) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(
            "SELECT a.id, a.student_id, a.course_id, " +
            "a.attendance_date, a.status, a.remarks " +
            "FROM attendance a " +
            "WHERE a.course_id=? AND a.attendance_date=? " +
            "ORDER BY a.student_id");
        ps.setInt(1, courseId);
        ps.setString(2, date);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Attendance att = new Attendance();
            att.setId(rs.getInt("id"));
            att.setStudentId(rs.getInt("student_id"));
            att.setCourseId(rs.getInt("course_id"));
            att.setStatus(rs.getString("status"));
            att.setRemarks(rs.getString("remarks"));
            list.add(att);
        }
        return list;
    }

    // ── check if already marked ──
    public int countMarked(int courseId, String date) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT COUNT(*) cnt FROM attendance " +
            "WHERE course_id=? AND attendance_date=?");
        ps.setInt(1, courseId);
        ps.setString(2, date);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt("cnt") : 0;
    }

    // ── student-wise attendance % per course ──
    public ResultSet getStudentAttendanceSummary(int courseId) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT s.id, s.name, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) AS present_count, " +
            "SUM(CASE WHEN a.status='Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
            "SUM(CASE WHEN a.status='Late'    THEN 1 ELSE 0 END) AS late_count, " +
            "ROUND((SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) " +
            "       / COUNT(*)) * 100, 2) AS pct " +
            "FROM attendance a " +
            "JOIN students s ON a.student_id = s.id " +
            "WHERE a.course_id = ? " +
            "GROUP BY s.id, s.name " +
            "ORDER BY pct DESC");
        ps.setInt(1, courseId);
        return ps.executeQuery();
    }

    // ── all students summary (all courses combined) ──
    public ResultSet getAllStudentsSummary() throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT s.id, s.name, c.course_name, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) AS present_count, " +
            "SUM(CASE WHEN a.status='Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
            "SUM(CASE WHEN a.status='Late'    THEN 1 ELSE 0 END) AS late_count, " +
            "ROUND((SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) " +
            "       / COUNT(*)) * 100, 2) AS pct " +
            "FROM attendance a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN courses  c ON a.course_id  = c.id " +
            "GROUP BY s.id, s.name, c.id, c.course_name " +
            "ORDER BY s.name, c.course_name");
        return ps.executeQuery();
    }

    // ── date-wise summary for a course ──
    public ResultSet getDateWiseSummary(int courseId,
                                        String from, String to) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT a.attendance_date, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) AS present_count, " +
            "SUM(CASE WHEN a.status='Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
            "SUM(CASE WHEN a.status='Late'    THEN 1 ELSE 0 END) AS late_count " +
            "FROM attendance a " +
            "WHERE a.course_id=? " +
            "AND a.attendance_date BETWEEN ? AND ? " +
            "GROUP BY a.attendance_date " +
            "ORDER BY a.attendance_date DESC");
        ps.setInt(1, courseId);
        ps.setString(2, from);
        ps.setString(3, to);
        return ps.executeQuery();
    }

    // ── defaulters (below threshold %) ──
    public ResultSet getDefaulters(double threshold) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT s.name, c.course_name, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) AS present_count, " +
            "ROUND((SUM(CASE WHEN a.status='Present' THEN 1 ELSE 0 END) " +
            "       / COUNT(*)) * 100, 2) AS pct " +
            "FROM attendance a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN courses  c ON a.course_id  = c.id " +
            "GROUP BY s.id, s.name, c.id, c.course_name " +
            "HAVING pct < ? " +
            "ORDER BY pct ASC");
        ps.setDouble(1, threshold);
        return ps.executeQuery();
    }

    // ── monthly calendar: per student per course ──
    public ResultSet getMonthlyCalendar(int studentId,
                                         int courseId,
                                         int year,
                                         int month) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT attendance_date, status FROM attendance " +
            "WHERE student_id=? AND course_id=? " +
            "AND YEAR(attendance_date)=? AND MONTH(attendance_date)=? " +
            "ORDER BY attendance_date");
        ps.setInt(1, studentId);
        ps.setInt(2, courseId);
        ps.setInt(3, year);
        ps.setInt(4, month);
        return ps.executeQuery();
    }

    // ── today's overall stats ──
    public ResultSet getTodayStats(String today) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN status='Present' THEN 1 ELSE 0 END) AS present_count, " +
            "SUM(CASE WHEN status='Absent'  THEN 1 ELSE 0 END) AS absent_count, " +
            "SUM(CASE WHEN status='Late'    THEN 1 ELSE 0 END) AS late_count " +
            "FROM attendance WHERE attendance_date=?");
        ps.setString(1, today);
        return ps.executeQuery();
    }

    // ── recent log ──
    public ResultSet getRecentLog(String courseFilter,
                                   int limit) throws SQLException {
        String q =
            "SELECT a.attendance_date, s.name, c.course_name, a.status " +
            "FROM attendance a " +
            "JOIN students s ON a.student_id = s.id " +
            "JOIN courses  c ON a.course_id  = c.id ";
        if (courseFilter != null && !courseFilter.isBlank())
            q += "WHERE c.course_name = ? ";
        q += "ORDER BY a.attendance_date DESC, s.name LIMIT " + limit;

        PreparedStatement ps = con.prepareStatement(q);
        if (courseFilter != null && !courseFilter.isBlank())
            ps.setString(1, courseFilter);
        return ps.executeQuery();
    }
}