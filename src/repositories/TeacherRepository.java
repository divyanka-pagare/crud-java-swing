package src.repositories;

import src.models.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherRepository {

    private final Connection con;

    public TeacherRepository(Connection con) {
        this.con = con;
    }

    public List<Teacher> getAll() throws SQLException {
        List<Teacher> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(
            "SELECT id, name, experience, specialization, available_time " +
            "FROM teachers ORDER BY name");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Teacher(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("experience"),
                rs.getString("specialization"),
                rs.getString("available_time")
            ));
        }
        return list;
    }

    public void insert(Teacher t) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO teachers (name, experience, specialization, available_time) " +
            "VALUES (?,?,?,?)");
        ps.setString(1, t.getName());
        ps.setInt(2, t.getExperience());
        ps.setString(3, t.getSpecialization());
        ps.setString(4, t.getAvailableTime());
        ps.executeUpdate();
    }

    public void update(Teacher t) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "UPDATE teachers SET name=?, experience=?, " +
            "specialization=?, available_time=? WHERE id=?");
        ps.setString(1, t.getName());
        ps.setInt(2, t.getExperience());
        ps.setString(3, t.getSpecialization());
        ps.setString(4, t.getAvailableTime());
        ps.setInt(5, t.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM teachers WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}