package src.services;

import src.models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class PaymentService {

    private final Connection con;

    public PaymentService(Connection con) {
        this.con = con;
    }

    public void savePaymentToDB(
            Student s,
            double total,
            double disc,
            double payable,
            String mode,
            int count,
            List<Integer> filteredCourseIds,
            javax.swing.table.DefaultTableModel courseTableModel
    ) {

        try {

            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO fee_payments " +
                            "(student_id, total_fees, discount_amt, amount_paid, " +
                            "payment_mode, payment_status) " +
                            "VALUES (?,?,?,?,?,'Paid')",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );

            pst.setInt(1, s.getId());
            pst.setDouble(2, total);
            pst.setDouble(3, disc);
            pst.setDouble(4, payable);
            pst.setString(5, mode);

            pst.executeUpdate();

            ResultSet keys = pst.getGeneratedKeys();

            int paymentId = -1;

            if (keys.next()) {
                paymentId = keys.getInt(1);
            }

            if (paymentId == -1) {
                return;
            }

            // ===== FILTERED COURSES =====
            if (filteredCourseIds != null
                    && !filteredCourseIds.isEmpty()) {

                saveFilteredCourses(
                        paymentId,
                        s.getId(),
                        filteredCourseIds
                );

            } else {

                saveCoursesFromTable(
                        paymentId,
                        s.getId(),
                        courseTableModel
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =====================================================
    // SAVE FILTERED COURSES
    // =====================================================
    private void saveFilteredCourses(
            int paymentId,
            int studentId,
            List<Integer> filteredCourseIds
    ) throws Exception {

        for (int courseId : filteredCourseIds) {

            if (!isCourseAlreadyPaid(studentId, courseId)) {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO fee_payment_courses " +
                                "(fee_payment_id, student_id, course_id) " +
                                "VALUES (?,?,?)"
                );

                ps.setInt(1, paymentId);
                ps.setInt(2, studentId);
                ps.setInt(3, courseId);

                ps.executeUpdate();
            }
        }
    }

    // =====================================================
    // SAVE COURSES FROM TABLE
    // =====================================================
    private void saveCoursesFromTable(
            int paymentId,
            int studentId,
            javax.swing.table.DefaultTableModel courseTableModel
    ) throws Exception {

        for (int i = 0; i < courseTableModel.getRowCount(); i++) {

            String courseName =
                    courseTableModel.getValueAt(i, 0).toString();

            PreparedStatement getCourse = con.prepareStatement(
                    "SELECT id FROM courses WHERE course_name=?"
            );

            getCourse.setString(1, courseName);

            ResultSet rs = getCourse.executeQuery();

            if (rs.next()) {

                int courseId = rs.getInt("id");

                if (!isCourseAlreadyPaid(studentId, courseId)) {

                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO fee_payment_courses " +
                                    "(fee_payment_id, student_id, course_id) " +
                                    "VALUES (?,?,?)"
                    );

                    ps.setInt(1, paymentId);
                    ps.setInt(2, studentId);
                    ps.setInt(3, courseId);

                    ps.executeUpdate();
                }
            }
        }
    }

    // =====================================================
    // CHECK DUPLICATE PAYMENT
    // =====================================================
    private boolean isCourseAlreadyPaid(
            int studentId,
            int courseId
    ) throws Exception {

        PreparedStatement checkStmt = con.prepareStatement(
                "SELECT id FROM fee_payment_courses " +
                        "WHERE student_id=? AND course_id=?"
        );

        checkStmt.setInt(1, studentId);
        checkStmt.setInt(2, courseId);

        ResultSet rs = checkStmt.executeQuery();

        return rs.next();
    }
}