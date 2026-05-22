package src.services;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ReceiptService {

    private final Connection con;

    public ReceiptService(Connection con) {
        this.con = con;
    }

    // =====================================================
    // LOAD PAYMENT HISTORY TABLE
    // =====================================================
    public void loadReceiptTable(
            DefaultTableModel receiptTableModel,
            String nameFilter
    ) {

        receiptTableModel.setRowCount(0);

        try {

            String query =
                    "SELECT fp.id, s.name, " +
                    "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                    "fp.total_fees, fp.discount_amt, " +
                    "fp.amount_paid, fp.payment_mode, " +
                    "fp.payment_status, fp.paid_at " +
                    "FROM fee_payments fp " +
                    "JOIN students s ON fp.student_id = s.id " +
                    "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                    "LEFT JOIN courses c ON fpc.course_id = c.id ";

            if (nameFilter != null && !nameFilter.isBlank()) {
                query += "WHERE s.name=? ";
            }

            query +=
                    "GROUP BY fp.id, s.name, fp.total_fees, " +
                    "fp.discount_amt, fp.amount_paid, " +
                    "fp.payment_mode, fp.payment_status, fp.paid_at " +
                    "ORDER BY fp.paid_at DESC";

            PreparedStatement pst =
                    con.prepareStatement(query);

            if (nameFilter != null && !nameFilter.isBlank()) {
                pst.setString(1, nameFilter);
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                Timestamp ts = rs.getTimestamp("paid_at");

                String date = ts != null
                        ? ts.toLocalDateTime().format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy HH:mm"
                                )
                        )
                        : "—";

                String courses =
                        rs.getString("courses");

                if (courses == null) {
                    courses = "—";
                }

                receiptTableModel.addRow(new Object[]{

                        rs.getInt("id"),

                        rs.getString("name"),

                        courses,

                        String.format(
                                "₹ %,.2f",
                                rs.getDouble("total_fees")
                        ),

                        rs.getDouble("discount_amt") > 0
                                ? String.format(
                                        "- ₹ %,.2f",
                                        rs.getDouble("discount_amt")
                                )
                                : "—",

                        String.format(
                                "₹ %,.2f",
                                rs.getDouble("amount_paid")
                        ),

                        rs.getString("payment_mode"),

                        rs.getString("payment_status"),

                        date
                });
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Failed to load payment history."
            );
        }
    }

    // =====================================================
    // POPULATE FILTER DROPDOWN
    // =====================================================
    public void populateFilterDropdown(
            JComboBox<String> filterDropdown
    ) {

        filterDropdown.removeAllItems();

        filterDropdown.addItem("All Students");

        try {

            PreparedStatement pst =
                    con.prepareStatement(
                            "SELECT DISTINCT s.name " +
                            "FROM fee_payments fp " +
                            "JOIN students s " +
                            "ON fp.student_id=s.id " +
                            "ORDER BY s.name"
                    );

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                filterDropdown.addItem(
                        rs.getString("name")
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }
}