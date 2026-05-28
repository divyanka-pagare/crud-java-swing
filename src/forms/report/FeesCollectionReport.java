package src.forms.report;

import src.db.DBConnection;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
// import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class FeesCollectionReport extends JFrame {

    // ===== FILTER CONTROLS =====
    JComboBox<String> viewByBox;

    // date wise
    JPanel      dateWisePanel;
    JSpinner    spinFrom, spinTo;

    // student wise
    JPanel            studentWisePanel;
    JComboBox<String> studentBox;
    JSpinner          spinStudentFrom, spinStudentTo;

    // course wise
    JPanel            courseWisePanel;
    JComboBox<String> courseBox;

    // ===== SUMMARY CARDS =====
    JLabel lblTotalCollection, lblTotalStudents,
           lblTotalCourses,    lblTotalPayments;

    // ===== MAIN REPORT TABLE =====
    JTable            reportTable;
    DefaultTableModel reportModel;

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    static final DateTimeFormatter DB_FMT   =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DISP_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public FeesCollectionReport() {

        setTitle("Fees Collection Report");
        setSize(1200, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = UIUtils.mainPanel();

        // ── Title ──
        JLabel title = UIUtils.bold("Fees Collection Report", 28);
        title.setBounds(30, 12, 500, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  FILTER BAR
        // ─────────────────────────────────────────
        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(
            new Color(210, 215, 220)));
        filterBar.setBounds(30, 62, 1120, 70);
        main.add(filterBar);

        JLabel lView = UIUtils.plain("View By:", 13);
        lView.setBounds(15, 22, 70, 26);
        filterBar.add(lView);

        viewByBox = new JComboBox<>(new String[]{
            "Date Wise", "Student Wise", "Course Wise"
        });
        viewByBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        viewByBox.setBounds(88, 20, 140, 32);
        filterBar.add(viewByBox);

        // ── Date Wise Panel ──
        dateWisePanel = new JPanel(null);
        dateWisePanel.setBackground(Color.WHITE);
        dateWisePanel.setBounds(245, 5, 700, 60);
        filterBar.add(dateWisePanel);

        JLabel lFrom = UIUtils.plain("From:", 13);
        lFrom.setBounds(0, 18, 45, 26);
        dateWisePanel.add(lFrom);

        spinFrom = dateSpinner();
        spinFrom.setBounds(48, 16, 145, 30);
        dateWisePanel.add(spinFrom);

        JLabel lTo = UIUtils.plain("To:", 13);
        lTo.setBounds(205, 18, 30, 26);
        dateWisePanel.add(lTo);

        spinTo = dateSpinner();
        spinTo.setBounds(238, 16, 145, 30);
        dateWisePanel.add(spinTo);

        JButton btnGen1 = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 398, 16, 110, 32);
        JButton btnClr1 = UIUtils.colorButton("Clear",
            UIUtils.CLR_GRAY, 518, 16, 80, 32);
        dateWisePanel.add(btnGen1);
        dateWisePanel.add(btnClr1);

        btnGen1.addActionListener(e -> generateReport());
        btnClr1.addActionListener(e -> {
            resetDateSpinner(spinFrom, -30);
            resetDateSpinner(spinTo,     0);
            generateReport();
        });

        // ── Student Wise Panel ──
        studentWisePanel = new JPanel(null);
        studentWisePanel.setBackground(Color.WHITE);
        studentWisePanel.setBounds(245, 5, 840, 60);
        studentWisePanel.setVisible(false);
        filterBar.add(studentWisePanel);

        JLabel lStu = UIUtils.plain("Student:", 13);
        lStu.setBounds(0, 18, 65, 26);
        studentWisePanel.add(lStu);

        studentBox = new JComboBox<>();
        studentBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentBox.setBounds(68, 16, 200, 32);
        studentWisePanel.add(studentBox);

        JLabel lSFrom = UIUtils.plain("From:", 13);
        lSFrom.setBounds(280, 18, 45, 26);
        studentWisePanel.add(lSFrom);

        spinStudentFrom = dateSpinner();
        spinStudentFrom.setBounds(328, 16, 145, 30);
        studentWisePanel.add(spinStudentFrom);

        JLabel lSTo = UIUtils.plain("To:", 13);
        lSTo.setBounds(485, 18, 30, 26);
        studentWisePanel.add(lSTo);

        spinStudentTo = dateSpinner();
        spinStudentTo.setBounds(518, 16, 145, 30);
        studentWisePanel.add(spinStudentTo);

        JButton btnGen2 = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 675, 16, 110, 32);
        studentWisePanel.add(btnGen2);
        btnGen2.addActionListener(e -> generateReport());

        // ── Course Wise Panel ──
        courseWisePanel = new JPanel(null);
        courseWisePanel.setBackground(Color.WHITE);
        courseWisePanel.setBounds(245, 5, 500, 60);
        courseWisePanel.setVisible(false);
        filterBar.add(courseWisePanel);

        JLabel lCrs = UIUtils.plain("Course:", 13);
        lCrs.setBounds(0, 18, 60, 26);
        courseWisePanel.add(lCrs);

        courseBox = new JComboBox<>();
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseBox.setBounds(64, 16, 260, 32);
        courseWisePanel.add(courseBox);

        JButton btnGen3 = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 338, 16, 110, 32);
        courseWisePanel.add(btnGen3);
        btnGen3.addActionListener(e -> generateReport());

        // ─────────────────────────────────────────
        //  SUMMARY CARDS
        // ─────────────────────────────────────────
        lblTotalCollection = summaryCard(main,
            "Total Collection", "₹ 0.00",
            new Color(0, 102, 204), 30, 145);
        lblTotalStudents = summaryCard(main,
            "Students Paid", "0",
            new Color(40, 167, 69), 305, 145);
        lblTotalCourses = summaryCard(main,
            "Courses Enrolled", "0",
            new Color(153, 0, 153), 580, 145);
        lblTotalPayments = summaryCard(main,
            "Total Transactions", "0",
            new Color(220, 53, 69), 855, 145);

        // ─────────────────────────────────────────
        //  REPORT TABLE
        // ─────────────────────────────────────────
        reportModel = new DefaultTableModel(
            new String[]{"#","Date","Student","Courses",
                         "Mode","Discount","Amount Paid","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = TableUtils.createStyledTable(reportModel);

        JScrollPane scroll = UIUtils.scrollPane(
            reportTable, 30, 240, 1120, 460);
        main.add(scroll);

        add(main);

        // ── Load dropdowns ──
        loadStudentDropdown();
        loadCourseDropdown();

        // ── Set default date ranges ──
        resetDateSpinner(spinFrom, -30);
        resetDateSpinner(spinTo,     0);
        setStudentDefaultDates();

        // ── View By listener ──
        viewByBox.addActionListener(e -> {
            String v = viewByBox.getSelectedItem().toString();
            dateWisePanel   .setVisible(v.equals("Date Wise"));
            studentWisePanel.setVisible(v.equals("Student Wise"));
            courseWisePanel .setVisible(v.equals("Course Wise"));
            updateTableColumns();
            generateReport();
        });

        // ── Student dropdown listener ──
        studentBox.addActionListener(e -> generateReport());

        // ── Course dropdown listener ──
        courseBox.addActionListener(e -> generateReport());

        generateReport();
        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  DATE SPINNER FACTORY
    // ─────────────────────────────────────────
    private JSpinner dateSpinner() {
        JSpinner s = new JSpinner(
            new SpinnerDateModel());
        JSpinner.DateEditor editor =
            new JSpinner.DateEditor(s, "dd-MM-yyyy");
        s.setEditor(editor);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return s;
    }

    private void resetDateSpinner(JSpinner spinner, int daysOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysOffset);
        spinner.setValue(cal.getTime());
    }

    private void setStudentDefaultDates() {
        // to = today, from = 1 year ago
        Calendar to = Calendar.getInstance();
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        spinStudentTo  .setValue(to.getTime());
        spinStudentFrom.setValue(from.getTime());
    }

    // ─────────────────────────────────────────
    //  LOAD DROPDOWNS
    // ─────────────────────────────────────────
    private void loadStudentDropdown() {
        studentBox.removeAllItems();
        studentBox.addItem("-- All Students --");
        try {
            pst = con.prepareStatement(
                "SELECT DISTINCT s.name FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "ORDER BY s.name");
            rs = pst.executeQuery();
            while (rs.next())
                studentBox.addItem(rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadCourseDropdown() {
        courseBox.removeAllItems();
        courseBox.addItem("-- All Courses --");
        try {
            pst = con.prepareStatement(
                "SELECT course_name FROM courses ORDER BY course_name");
            rs = pst.executeQuery();
            while (rs.next())
                courseBox.addItem(rs.getString("course_name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  GET DATE FROM SPINNER
    // ─────────────────────────────────────────
    private String getSpinnerDate(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(d);
    }

    // ─────────────────────────────────────────
    //  UPDATE COLUMNS
    // ─────────────────────────────────────────
    private void updateTableColumns() {
        String view = viewByBox.getSelectedItem().toString();
        reportModel.setRowCount(0);
        switch (view) {
            case "Date Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Date","Student","Courses",
                    "Mode","Discount","Amount Paid","Status"});
                break;
            case "Student Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Payment Date","Courses Paid",
                    "Mode","Discount","Amount Paid","Status"});
                break;
            case "Course Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Student","Payment Date",
                    "Mode","Amount Paid","Status"});
                break;
        }
    }

    // ─────────────────────────────────────────
    //  GENERATE REPORT
    // ─────────────────────────────────────────
    public void generateReport() {
        String view = viewByBox.getSelectedItem().toString();
        updateTableColumns();

        switch (view) {
            case "Date Wise":
                loadDateWise(
                    getSpinnerDate(spinFrom),
                    getSpinnerDate(spinTo));
                break;
            case "Student Wise":
                loadStudentWise(
                    studentBox.getSelectedItem() != null
                        ? studentBox.getSelectedItem().toString()
                        : "",
                    getSpinnerDate(spinStudentFrom),
                    getSpinnerDate(spinStudentTo));
                break;
            case "Course Wise":
                loadCourseWise(
                    courseBox.getSelectedItem() != null
                        ? courseBox.getSelectedItem().toString()
                        : "");
                break;
        }

        updateSummaryCards();
        TableUtils.resizeColumnWidth(reportTable);
    }

    // ─────────────────────────────────────────
    //  DATE WISE
    // ─────────────────────────────────────────
    private void loadDateWise(String from, String to) {
        try {
            String q =
                "SELECT fp.paid_at, s.name, " +
                "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                "fp.payment_mode, fp.discount_amt, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                "LEFT JOIN courses c ON fpc.course_id = c.id " +
                "WHERE DATE(fp.paid_at) BETWEEN ? AND ? " +
                "GROUP BY fp.id, fp.paid_at, s.name, fp.payment_mode, " +
                "fp.discount_amt, fp.amount_paid, fp.payment_status " +
                "ORDER BY fp.paid_at DESC";

            pst = con.prepareStatement(q);
            pst.setString(1, from);
            pst.setString(2, to);
            rs = pst.executeQuery();

            int row = 1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date = ts != null
                    ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                double disc = rs.getDouble("discount_amt");
                reportModel.addRow(new Object[]{
                    row++, date,
                    rs.getString("s.name"),
                    rs.getString("courses") != null ? rs.getString("courses") : "—",
                    rs.getString("payment_mode"),
                    disc > 0 ? String.format("- ₹ %,.2f", disc) : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  STUDENT WISE
    // ─────────────────────────────────────────
    private void loadStudentWise(String studentName,
                                  String from, String to) {
        try {
            StringBuilder q = new StringBuilder(
                "SELECT fp.paid_at, " +
                "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                "fp.payment_mode, fp.discount_amt, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                "LEFT JOIN courses c ON fpc.course_id = c.id " +
                "WHERE DATE(fp.paid_at) BETWEEN ? AND ? ");

            if (!studentName.isEmpty() &&
                !studentName.equals("-- All Students --"))
                q.append("AND s.name = ? ");

            q.append("GROUP BY fp.id, fp.paid_at, fp.payment_mode, " +
                     "fp.discount_amt, fp.amount_paid, fp.payment_status " +
                     "ORDER BY fp.paid_at DESC");

            pst = con.prepareStatement(q.toString());
            pst.setString(1, from);
            pst.setString(2, to);
            if (!studentName.isEmpty() &&
                !studentName.equals("-- All Students --"))
                pst.setString(3, studentName);

            rs = pst.executeQuery();

            int row = 1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date = ts != null
                    ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                double disc = rs.getDouble("discount_amt");
                reportModel.addRow(new Object[]{
                    row++, date,
                    rs.getString("courses") != null ? rs.getString("courses") : "—",
                    rs.getString("payment_mode"),
                    disc > 0 ? String.format("- ₹ %,.2f", disc) : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  COURSE WISE
    // ─────────────────────────────────────────
    private void loadCourseWise(String courseName) {
        try {
            StringBuilder q = new StringBuilder(
                "SELECT s.name, fp.paid_at, " +
                "fp.payment_mode, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                "JOIN courses c ON fpc.course_id = c.id " +
                "WHERE 1=1 ");

            if (!courseName.isEmpty() &&
                !courseName.equals("-- All Courses --"))
                q.append("AND c.course_name = ? ");

            q.append("ORDER BY fp.paid_at DESC");

            pst = con.prepareStatement(q.toString());
            if (!courseName.isEmpty() &&
                !courseName.equals("-- All Courses --"))
                pst.setString(1, courseName);

            rs = pst.executeQuery();

            int row = 1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date = ts != null
                    ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                reportModel.addRow(new Object[]{
                    row++,
                    rs.getString("s.name"),
                    date,
                    rs.getString("payment_mode"),
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  SUMMARY CARDS
    // ─────────────────────────────────────────
    private void updateSummaryCards() {
        try {
            pst = con.prepareStatement(
                "SELECT " +
                "SUM(fp.amount_paid)           AS total, " +
                "COUNT(DISTINCT fp.student_id) AS students, " +
                "COUNT(DISTINCT fpc.course_id) AS courses, " +
                "COUNT(fp.id)                  AS transactions " +
                "FROM fee_payments fp " +
                "LEFT JOIN fee_payment_courses fpc " +
                "  ON fpc.fee_payment_id = fp.id");
            rs = pst.executeQuery();
            if (rs.next()) {
                lblTotalCollection.setText(
                    String.format("₹ %,.2f", rs.getDouble("total")));
                lblTotalStudents.setText(
                    String.valueOf(rs.getInt("students")));
                lblTotalCourses.setText(
                    String.valueOf(rs.getInt("courses")));
                lblTotalPayments.setText(
                    String.valueOf(rs.getInt("transactions")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  SUMMARY CARD HELPER
    // ─────────────────────────────────────────
    private JLabel summaryCard(JPanel parent, String heading,
                                String value, Color color,
                                int x, int y) {
        JPanel card = new JPanel(null);
        card.setBackground(color);
        card.setBounds(x, y, 245, 80);
        parent.add(card);

        JLabel lHead = new JLabel(heading);
        lHead.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lHead.setForeground(new Color(220, 220, 220));
        lHead.setBounds(14, 10, 220, 20);
        card.add(lHead);

        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lVal.setForeground(Color.WHITE);
        lVal.setBounds(14, 34, 220, 32);
        card.add(lVal);

        return lVal;
    }
}