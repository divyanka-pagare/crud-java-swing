package src.repositories;

import src.models.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private final Connection con;

    public CourseRepository(Connection con) {
        this.con = con;
    }

    public List<Course> getAll() throws SQLException {
        List<Course> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(
            "SELECT id, course_name, fees, duration FROM courses ORDER BY course_name");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Course(
                rs.getInt("id"),
                rs.getString("course_name"),
                rs.getDouble("fees"),
                rs.getString("duration")
            ));
        }
        return list;
    }

    public void insert(Course c) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO courses (course_name, fees, duration) VALUES (?,?,?)");
        ps.setString(1, c.getCourseName());
        ps.setDouble(2, c.getFees());
        ps.setString(3, c.getDuration());
        ps.executeUpdate();
    }

    public void update(Course c) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "UPDATE courses SET course_name=?, fees=?, duration=? WHERE id=?");
        ps.setString(1, c.getCourseName());
        ps.setDouble(2, c.getFees());
        ps.setString(3, c.getDuration());
        ps.setInt(4, c.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM courses WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public boolean existsByName(String name, int excludeId) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT id FROM courses WHERE course_name=? AND id!=?");
        ps.setString(1, name);
        ps.setInt(2, excludeId);
        return ps.executeQuery().next();
    }
}