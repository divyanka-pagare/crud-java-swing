package src.repositories;

import java.sql.*;
// import java.util.ArrayList;
import java.util.List;

import src.models.Student;

public class EnrollmentRepository {

    private final Connection con;

    public EnrollmentRepository(Connection con) {
        this.con = con;
    }

    public ResultSet getUnpaidCourses(
            Student student,
            List<Integer> filteredCourseIds
    ) throws SQLException {

        PreparedStatement ps;

        if (filteredCourseIds != null && !filteredCourseIds.isEmpty()) {

            StringBuilder inClause = new StringBuilder();

            for (int i = 0; i < filteredCourseIds.size(); i++) {
                inClause.append(i == 0 ? "?" : ",?");
            }

            String query =
                    "SELECT c.id, c.course_name, c.duration, c.fees " +
                    "FROM enrollments e " +
                    "JOIN courses c ON e.course_id = c.id " +
                    "WHERE e.student_id = ? " +
                    "AND c.id IN (" + inClause + ") " +
                    "AND c.id NOT IN ( " +
                    "   SELECT fpc.course_id " +
                    "   FROM fee_payment_courses fpc " +
                    "   WHERE fpc.student_id = ? " +
                    ")";

            ps = con.prepareStatement(query);

            ps.setInt(1, student.getId());

            int index = 2;

            for (Integer courseId : filteredCourseIds) {
                ps.setInt(index++, courseId);
            }

            ps.setInt(index, student.getId());

        } else {

            String query =
                    "SELECT c.id, c.course_name, c.duration, c.fees " +
                    "FROM enrollments e " +
                    "JOIN courses c ON e.course_id = c.id " +
                    "WHERE e.student_id = ? " +
                    "AND c.id NOT IN ( " +
                    "   SELECT fpc.course_id " +
                    "   FROM fee_payment_courses fpc " +
                    "   WHERE fpc.student_id = ? " +
                    ")";

            ps = con.prepareStatement(query);

            ps.setInt(1, student.getId());
            ps.setInt(2, student.getId());
        }

        return ps.executeQuery();
    }
} 