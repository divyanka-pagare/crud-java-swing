package src.forms.transaction;

import src.db.DBConnection;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentEnquiryForm extends JFrame {

    JTextField     txtSearch;
    JComboBox<String> searchByBox;
    JButton        searchBtn, clearBtn;

    // ===== STUDENT DETAIL LABELS =====
    JLabel lblId, lblName, lblEmail, lblPhone;
    JLabel lblGender, lblAge, lblCountry, lblSkills;
    JLabel lblAddress, lblBio;

    // ===== ENROLLED COURSES TABLE =====
    JTable            courseTable;
    DefaultTableModel courseModel;

    // ===== PAYMENT HISTORY TABLE =====
    JTable            paymentTable;
    DefaultTableModel paymentModel;

    // ===== RESULTS TABLE =====
    JTable            resultsTable;
    DefaultTableModel resultsModel;

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    public StudentEnquiryForm() {

        setTitle("Student Enquiry");
        setSize(1100, 720);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 247, 250));

        // ── Title ──
        JLabel title = UIUtils.bold("Student Enquiry", 26);
        title.setBounds(30, 12, 400, 40);
        main.add(title);

        // ── Search Bar ──
        JLabel lSearchBy = UIUtils.plain("Search By:", 13);
        lSearchBy.setBounds(30, 65, 90, 30);
        main.add(lSearchBy);

        searchByBox = new JComboBox<>(new String[]{
            "Name", "Email", "Phone", "ID"
        });
        searchByBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchByBox.setBounds(125, 65, 110, 32);
        main.add(searchByBox);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBounds(245, 65, 260, 32);
        txtSearch.setToolTipText("Enter search term");
        main.add(txtSearch);

        searchBtn = UIUtils.colorButton("SEARCH",
            new Color(0, 120, 215), 515, 65, 100, 32);
        clearBtn  = UIUtils.colorButton("CLEAR",
            new Color(108, 117, 125), 625, 65, 100, 32);
        main.add(searchBtn);
        main.add(clearBtn);

        // ── Search Results Table ──
        JLabel lResults = UIUtils.bold("Search Results:", 13);
        lResults.setBounds(30, 108, 200, 25);
        main.add(lResults);

        String[] rCols = {"ID", "Name", "Email", "Phone",
                          "Gender", "Age", "Country"};
        resultsModel = new DefaultTableModel(rCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultsTable = TableUtils.createStyledTable(resultsModel);

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBounds(30, 135, 700, 150);
        resultsScroll.setBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)));
        main.add(resultsScroll);

        // ── Student Detail Card ──
        JPanel detailCard = new JPanel(null);
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)),
            "Student Details",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        detailCard.setBounds(30, 295, 700, 200);
        main.add(detailCard);

        lblId      = detailLabel("ID       : —");
        lblName    = detailLabel("Name     : —");
        lblEmail   = detailLabel("Email    : —");
        lblPhone   = detailLabel("Phone    : —");
        lblGender  = detailLabel("Gender   : —");
        lblAge     = detailLabel("Age      : —");
        lblCountry = detailLabel("Country  : —");
        lblSkills  = detailLabel("Skills   : —");
        lblAddress = detailLabel("Address  : —");
        lblBio     = detailLabel("Bio      : —");

        // left column
        lblId     .setBounds(10,  22, 330, 18);
        lblName   .setBounds(10,  42, 330, 18);
        lblEmail  .setBounds(10,  62, 330, 18);
        lblPhone  .setBounds(10,  82, 330, 18);
        lblGender .setBounds(10, 102, 330, 18);

        // right column
        lblAge    .setBounds(350,  22, 330, 18);
        lblCountry.setBounds(350,  42, 330, 18);
        lblSkills .setBounds(350,  62, 330, 18);
        lblAddress.setBounds(350,  82, 330, 18);
        lblBio    .setBounds(350, 102, 330, 18);

        detailCard.add(lblId);     detailCard.add(lblName);
        detailCard.add(lblEmail);  detailCard.add(lblPhone);
        detailCard.add(lblGender); detailCard.add(lblAge);
        detailCard.add(lblCountry);detailCard.add(lblSkills);
        detailCard.add(lblAddress);detailCard.add(lblBio);

        // ── Enrolled Courses ──
        JLabel lCourses = UIUtils.bold("Enrolled Courses:", 13);
        lCourses.setBounds(30, 504, 200, 25);
        main.add(lCourses);

        String[] cCols = {"Course", "Duration", "Fees (₹)", "Enrolled On"};
        courseModel = new DefaultTableModel(cCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        courseTable = TableUtils.createStyledTable(courseModel);

        JScrollPane courseScroll = new JScrollPane(courseTable);
        courseScroll.setBounds(30, 530, 700, 140);
        courseScroll.setBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)));
        main.add(courseScroll);

        // ── RIGHT SIDE: Payment Summary ──
        JLabel lPayment = UIUtils.bold("Payment History:", 13);
        lPayment.setBounds(750, 108, 300, 25);
        main.add(lPayment);

        String[] pCols = {
            "ID", "Courses Paid", "Amount", "Mode", "Status", "Date"
        };
        paymentModel = new DefaultTableModel(pCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        paymentTable = TableUtils.createStyledTable(paymentModel);

        JScrollPane paymentScroll = new JScrollPane(paymentTable);
        paymentScroll.setBounds(750, 135, 310, 535);
        paymentScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paymentScroll.setBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)));
        main.add(paymentScroll);

        add(main);

        // ── Listeners ──
        searchBtn.addActionListener(e -> searchStudent());
        clearBtn .addActionListener(e -> clearAll());

        // click row in results to load details
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = resultsTable.getSelectedRow();
                if (row == -1) return;
                int id = Integer.parseInt(
                    resultsModel.getValueAt(row, 0).toString());
                loadStudentDetails(id);
            }
        });

        // press Enter in search box
        txtSearch.addActionListener(e -> searchStudent());

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────
    public void searchStudent() {

        String keyword  = txtSearch.getText().trim();
        String searchBy = searchByBox.getSelectedItem().toString();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a search term");
            return;
        }

        resultsModel.setRowCount(0);
        clearDetails();

        try {
            String col;
            switch (searchBy) {
                case "Email":  col = "email";  break;
                case "Phone":  col = "phone";  break;
                case "ID":     col = "id";     break;
                default:       col = "name";   break;
            }

            String query;
            if (searchBy.equals("ID")) {
                query = "SELECT id,name,email,phone,gender,age,country " +
                        "FROM students WHERE id=?";
            } else {
                query = "SELECT id,name,email,phone,gender,age,country " +
                        "FROM students WHERE " + col + " LIKE ?";
                keyword = "%" + keyword + "%";
            }

            pst = con.prepareStatement(query);
            pst.setString(1, keyword);
            rs = pst.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                resultsModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("gender"),
                    rs.getInt("age"),
                    rs.getString("country")
                });
            }

            if (!found) {
                JOptionPane.showMessageDialog(this,
                    "No student found for: " + txtSearch.getText().trim());
            }

            TableUtils.resizeColumnWidth(resultsTable);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  LOAD STUDENT DETAILS ON ROW CLICK
    // ─────────────────────────────────────────
    public void loadStudentDetails(int studentId) {

        try {
            // full student info
            pst = con.prepareStatement(
                "SELECT * FROM students WHERE id=?");
            pst.setInt(1, studentId);
            rs = pst.executeQuery();

            if (rs.next()) {
                lblId     .setText("ID       : " + rs.getInt("id"));
                lblName   .setText("Name     : " + rs.getString("name"));
                lblEmail  .setText("Email    : " + rs.getString("email"));
                lblPhone  .setText("Phone    : " + rs.getString("phone"));
                lblGender .setText("Gender   : " + rs.getString("gender"));
                lblAge    .setText("Age      : " + rs.getInt("age"));
                lblCountry.setText("Country  : " + rs.getString("country"));
                lblSkills .setText("Skills   : " + rs.getString("skills"));
                lblAddress.setText("Address  : " + rs.getString("address"));
                lblBio    .setText("Bio      : " + rs.getString("bio"));
            }

            // enrolled courses
            courseModel.setRowCount(0);
            pst = con.prepareStatement(
                "SELECT c.course_name, c.duration, c.fees, e.enrolled_at " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.id " +
                "WHERE e.student_id = ? " +
                "ORDER BY e.enrolled_at DESC");
            pst.setInt(1, studentId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("enrolled_at");
                String date = ts != null
                    ? ts.toLocalDateTime().format(
                        java.time.format.DateTimeFormatter
                            .ofPattern("dd-MM-yyyy HH:mm"))
                    : "—";
                courseModel.addRow(new Object[]{
                    rs.getString("course_name"),
                    rs.getString("duration"),
                    String.format("₹ %,.2f", rs.getDouble("fees")),
                    date
                });
            }

            TableUtils.resizeColumnWidth(courseTable);

            // payment history
            paymentModel.setRowCount(0);
            pst = con.prepareStatement(
                "SELECT fp.id, " +
                "GROUP_CONCAT(c.course_name SEPARATOR ', ') AS courses, " +
                "fp.amount_paid, fp.payment_mode, " +
                "fp.payment_status, fp.paid_at " +
                "FROM fee_payments fp " +
                "LEFT JOIN fee_payment_courses fpc " +
                "  ON fpc.fee_payment_id = fp.id " +
                "LEFT JOIN courses c ON fpc.course_id = c.id " +
                "WHERE fp.student_id = ? " +
                "GROUP BY fp.id, fp.amount_paid, fp.payment_mode, " +
                "fp.payment_status, fp.paid_at " +
                "ORDER BY fp.paid_at DESC");
            pst.setInt(1, studentId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date = ts != null
                    ? ts.toLocalDateTime().format(
                        java.time.format.DateTimeFormatter
                            .ofPattern("dd-MM-yyyy HH:mm"))
                    : "—";
                String courses = rs.getString("courses");
                paymentModel.addRow(new Object[]{
                    rs.getInt("fp.id"),
                    courses != null ? courses : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_mode"),
                    rs.getString("payment_status"),
                    date
                });
            }

            TableUtils.resizeColumnWidth(paymentTable);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────
    public void clearAll() {
        txtSearch.setText("");
        resultsModel.setRowCount(0);
        courseModel.setRowCount(0);
        paymentModel.setRowCount(0);
        clearDetails();
    }

    private void clearDetails() {
        lblId     .setText("ID       : —");
        lblName   .setText("Name     : —");
        lblEmail  .setText("Email    : —");
        lblPhone  .setText("Phone    : —");
        lblGender .setText("Gender   : —");
        lblAge    .setText("Age      : —");
        lblCountry.setText("Country  : —");
        lblSkills .setText("Skills   : —");
        lblAddress.setText("Address  : —");
        lblBio    .setText("Bio      : —");
    }

    private JLabel detailLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(60, 60, 70));
        return l;
    }
}