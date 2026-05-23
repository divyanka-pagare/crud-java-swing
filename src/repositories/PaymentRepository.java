package src.repositories;

import java.sql.*;

public class PaymentRepository {

    private final Connection con;

    public PaymentRepository(Connection con) {
        this.con = con;
    }

    public ResultSet getReceiptHistory(String nameFilter)
            throws SQLException {

        String query =
                "SELECT fp.id, s.name, " +
                "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                "fp.total_fees, fp.discount_amt, " +
                "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                "LEFT JOIN courses c ON fpc.course_id = c.id ";

        if (nameFilter != null && !nameFilter.isBlank()) {
            query += "WHERE s.name = ? ";
        }

        query +=
                "GROUP BY fp.id, s.name, fp.total_fees, fp.discount_amt, " +
                "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
                "ORDER BY fp.paid_at DESC";

        PreparedStatement pst = con.prepareStatement(query);

        if (nameFilter != null && !nameFilter.isBlank()) {
            pst.setString(1, nameFilter);
        }

        return pst.executeQuery();
    }

    public ResultSet getPaymentStudents()
            throws SQLException {

        String query =
                "SELECT DISTINCT s.name " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "ORDER BY s.name";

        PreparedStatement pst = con.prepareStatement(query);

        return pst.executeQuery();
    }

    public ResultSet getLatestPayment(int studentId)
            throws SQLException {

        PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM fee_payments " +
                "WHERE student_id = ? " +
                "ORDER BY paid_at DESC LIMIT 1"
        );

        pst.setInt(1, studentId);

        return pst.executeQuery();
    }

    public ResultSet getLatestPaymentByStudentId(int studentId) throws SQLException {

        String query =
            "SELECT * FROM fee_payments " +
            "WHERE student_id=? " +
            "ORDER BY paid_at DESC LIMIT 1";
    
        PreparedStatement pst = con.prepareStatement(query);
    
        pst.setInt(1, studentId);
    
        return pst.executeQuery();
    }
    
    public ResultSet getAllReceipts(String nameFilter) throws SQLException {
    
        String query =
            "SELECT fp.id, s.name, " +
            "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
            "fp.total_fees, fp.discount_amt, " +
            "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
            "FROM fee_payments fp " +
            "JOIN students s ON fp.student_id = s.id " +
            "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
            "LEFT JOIN courses c ON fpc.course_id = c.id ";
    
        if (nameFilter != null && !nameFilter.isBlank()) {
            query += "WHERE s.name=? ";
        }
    
        query +=
            "GROUP BY fp.id, s.name, fp.total_fees, fp.discount_amt, " +
            "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
            "ORDER BY fp.paid_at DESC";
    
        PreparedStatement pst = con.prepareStatement(query);
    
        if (nameFilter != null && !nameFilter.isBlank()) {
            pst.setString(1, nameFilter);
        }
    
        return pst.executeQuery();
    }
    
    public ResultSet getStudentsWithPayments() throws SQLException {
    
        String query =
            "SELECT DISTINCT s.name " +
            "FROM fee_payments fp " +
            "JOIN students s ON fp.student_id=s.id " +
            "ORDER BY s.name";
    
        PreparedStatement pst = con.prepareStatement(query);
    
        return pst.executeQuery();
    }
    
    public void deletePayment(int paymentId) throws SQLException {
    
        PreparedStatement pst = con.prepareStatement(
            "DELETE FROM fee_payment_courses WHERE fee_payment_id = ?"
        );
    
        pst.setInt(1, paymentId);
    
        pst.executeUpdate();
    
        pst = con.prepareStatement(
            "DELETE FROM fee_payments WHERE id = ?"
        );
    
        pst.setInt(1, paymentId);
    
        pst.executeUpdate();
    }
}