package src.services;

import src.models.Student;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.Connection;

import java.util.ArrayList;
import java.util.List;

public class StudentService {


    private Connection con;

    public StudentService(Connection con) {
        this.con = con;
    }

    public List<Student> getAllStudents() {

        List<Student> students = new ArrayList<>();

        String query =
            "SELECT id, name, email, phone, gender, skills, " +
            "country, age, address, bio " +
            "FROM students ORDER BY name";

        try (
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery()
        ) {

            while (rs.next()) {

                Student student = new Student(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("gender"),
                    rs.getString("skills"),
                    rs.getString("country"),
                    rs.getInt("age"),
                    rs.getString("address"),
                    rs.getString("bio")
                );

                students.add(student);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return students;
    }

    public void loadStudents(JComboBox<Student> studentDropdown) {

        studentDropdown.removeAllItems();
    
        studentDropdown.addItem(null);
    
        String query =
            "SELECT id, name, email, phone, gender, skills, " +
            "country, age, address, bio " +
            "FROM students ORDER BY name";
    
        try (
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery()
        ) {
    
            while (rs.next()) {
    
                Student student = new Student(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("gender"),
                    rs.getString("skills"),
                    rs.getString("country"),
                    rs.getInt("age"),
                    rs.getString("address"),
                    rs.getString("bio")
                );
    
                studentDropdown.addItem(student);
            }
    
        } catch (Exception e) {
    
            e.printStackTrace();
        }
    }
}

