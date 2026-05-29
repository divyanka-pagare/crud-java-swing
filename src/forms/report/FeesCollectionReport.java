package src.forms.report;

import src.db.DBConnection;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
// import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

// import java.util.HashMap;
// import java.util.Map;

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

    JScrollPane reportScroll;

    // ===== COURSE ANALYSIS PANEL =====
    JPanel  courseAnalysisPanel;
    JLabel  lblCourseStudents, lblCourseRevenue,
            lblTotalRevenue,   lblCoursePercent,
            lblCoursePercentBar;

    // ===== SUMMARY CARDS =====
    JLabel lblTotalCollection, lblTotalStudents,
           lblTotalCourses,    lblTotalPayments;

    // ===== ANALYTICS PANELS =====
    JPanel analyticsPanel;
    
    // ===== INSIGHT LABELS =====
    JLabel lblHighestDay;
    JLabel lblAverageDay;
    JLabel lblPendingPayments;
    JLabel lblTopCourse;
    
    // ===== STUDENT SUMMARY =====
    JPanel studentSummaryPanel;
    
    JLabel lblStudentTotalPaid;
    JLabel lblStudentTotalDiscount;
    JLabel lblStudentPending;
    JLabel lblStudentLastPayment;
    
    // ===== COURSE EXTRA ANALYTICS =====
    JLabel lblAverageRevenuePerStudent;
    JLabel lblTopPaymentMode;
    
           
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

        // ==========================================================
        // ANALYTICS PANEL
        // ==========================================================
        
        analyticsPanel = new JPanel(null);
        analyticsPanel.setBackground(Color.WHITE);
        analyticsPanel.setBorder(BorderFactory.createLineBorder(
                new Color(210,215,220)));
        
        analyticsPanel.setBounds(30, 240, 1120, 180);
        
        main.add(analyticsPanel);
        

        // ==========================================================
        // STUDENT SUMMARY PANEL
        // ==========================================================

        studentSummaryPanel = new JPanel(null);

        studentSummaryPanel.setBackground(Color.WHITE);

        studentSummaryPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(210,215,220)
                        ),
                        "Student Financial Summary"
                )
        );

        studentSummaryPanel.setBounds(
                30,
                340,
                1120,
                100
        );

        studentSummaryPanel.setVisible(false);

        main.add(studentSummaryPanel);

        lblStudentTotalPaid = summaryText(
                studentSummaryPanel,
                "Total Paid",
                "₹ 0",
                20
        );

        lblStudentTotalDiscount = summaryText(
                studentSummaryPanel,
                "Discount",
                "₹ 0",
                300
        );

        lblStudentPending = summaryText(
                studentSummaryPanel,
                "Pending",
                "₹ 0",
                580
        );

        lblStudentLastPayment = summaryText(
                studentSummaryPanel,
                "Last Payment",
                "-",
                860
        );
        // ─────────────────────────────────────────
        // INSIGHT CARDS
        // ─────────────────────────────────────────
        
        lblHighestDay = createAnalyticsCard(
                analyticsPanel,
                "Highest Collection Day",
                "₹ 0",
                new Color(0,102,204),
                20,
                20
        );
        
        lblAverageDay = createAnalyticsCard(
                analyticsPanel,
                "Average Collection",
                "₹ 0",
                new Color(40,167,69),
                290,
                20
        );
        
        lblPendingPayments = createAnalyticsCard(
                analyticsPanel,
                "Pending Payments",
                "0",
                new Color(220,53,69),
                560,
                20
        );
        
        lblTopCourse = createAnalyticsCard(
                analyticsPanel,
                "Top Course",
                "-",
                new Color(153,0,153),
                830,
                20
        );

        // ─────────────────────────────────────────
        //  COURSE ANALYSIS PANEL (visible only in Course Wise)
        // ─────────────────────────────────────────
        courseAnalysisPanel = new JPanel(null);
        courseAnalysisPanel.setBackground(Color.WHITE);
        courseAnalysisPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            "Course Analysis",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        courseAnalysisPanel.setBounds(30, 240, 1120, 110);
        courseAnalysisPanel.setVisible(false);
        main.add(courseAnalysisPanel);

        // ── Students in course ──
        JLabel lCS = UIUtils.plain("Students Enrolled:", 12);
        lCS.setBounds(14, 24, 150, 20);
        courseAnalysisPanel.add(lCS);
        lblCourseStudents = UIUtils.bold("0", 18);
        lblCourseStudents.setForeground(new Color(0, 102, 204));
        lblCourseStudents.setBounds(14, 44, 150, 28);
        courseAnalysisPanel.add(lblCourseStudents);

        // ── Course revenue ──
        JLabel lCR = UIUtils.plain("Course Revenue:", 12);
        lCR.setBounds(200, 24, 150, 20);
        courseAnalysisPanel.add(lCR);
        lblCourseRevenue = UIUtils.bold("₹ 0.00", 18);
        lblCourseRevenue.setForeground(new Color(40, 167, 69));
        lblCourseRevenue.setBounds(200, 44, 200, 28);
        courseAnalysisPanel.add(lblCourseRevenue);

        // ── Total revenue of institute ──
        JLabel lTR = UIUtils.plain("Total Institute Revenue:", 12);
        lTR.setBounds(440, 24, 180, 20);
        courseAnalysisPanel.add(lTR);
        lblTotalRevenue = UIUtils.bold("₹ 0.00", 18);
        lblTotalRevenue.setForeground(new Color(153, 0, 153));
        lblTotalRevenue.setBounds(440, 44, 200, 28);
        courseAnalysisPanel.add(lblTotalRevenue);

        // ── Percentage contribution ──
        JLabel lCP = UIUtils.plain("Contribution to Total Revenue:", 12);
        lCP.setBounds(680, 24, 220, 20);
        courseAnalysisPanel.add(lCP);
        lblCoursePercent = UIUtils.bold("0.00%", 22);
        lblCoursePercent.setForeground(new Color(220, 53, 69));
        lblCoursePercent.setBounds(680, 44, 160, 30);
        courseAnalysisPanel.add(lblCoursePercent);

        // ── Progress bar (drawn as colored panel) ──
        JLabel lBarBg = new JLabel();
        lBarBg.setBackground(new Color(230, 230, 230));
        lBarBg.setOpaque(true);
        lBarBg.setBounds(680, 78, 400, 18);
        courseAnalysisPanel.add(lBarBg);

        lblCoursePercentBar = new JLabel();
        lblCoursePercentBar.setBackground(new Color(220, 53, 69));
        lblCoursePercentBar.setOpaque(true);
        lblCoursePercentBar.setBounds(680, 78, 0, 18);
        courseAnalysisPanel.add(lblCoursePercentBar);

        // ─────────────────────────────────────────
        //  REPORT TABLE
        // ─────────────────────────────────────────
        reportModel = new DefaultTableModel(
            new String[]{"#","Date","Student","Courses",
                         "Mode","Discount","Amount Paid","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = TableUtils.createStyledTable(reportModel);


        reportScroll = UIUtils.scrollPane(reportTable, 30, 240, 1120, 460);
        main.add(reportScroll);

        add(main);

        reportTable.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {

                Component c =
                        super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                String val =
                        value != null
                                ? value.toString()
                                : "";

                if(val.equalsIgnoreCase("Paid")) {

                    c.setForeground(
                            new Color(40,167,69)
                    );
                }
                else if(val.equalsIgnoreCase("Pending")) {

                    c.setForeground(
                            new Color(220,53,69)
                    );
                }
                else {

                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        }
);

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
            courseAnalysisPanel .setVisible(v.equals("Course Wise"));
            updateTableColumns();
            generateReport();
        });

        // ── Student dropdown listener ──
        studentBox.addActionListener(e -> generateReport());

        // ── Course dropdown listener ──
        courseBox.addActionListener(e -> {
            generateReport();
            if (viewByBox.getSelectedItem().toString().equals("Course Wise")) {
                updateCourseAnalysis();
            }
        });

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

    // ==========================================================
// ANALYTICS CARD
// ==========================================================

private JLabel createAnalyticsCard(
        JPanel parent,
        String title,
        String value,
        Color color,
        int x,
        int y
) {

    JPanel card = new JPanel(null);

    card.setBackground(color);

    card.setBounds(x, y, 240, 120);

    parent.add(card);

    JLabel lblTitle = new JLabel(title);

    lblTitle.setForeground(Color.WHITE);

    lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

    lblTitle.setBounds(15, 15, 200, 20);

    card.add(lblTitle);

    JLabel lblValue = new JLabel(value);

    lblValue.setForeground(Color.WHITE);

    lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));

    lblValue.setBounds(15, 50, 210, 35);

    card.add(lblValue);

    return lblValue;
}

// ==========================================================
// SUMMARY TEXT
// ==========================================================

private JLabel summaryText(
        JPanel panel,
        String heading,
        String value,
        int x
) {

    JLabel h = new JLabel(heading);

    h.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    h.setBounds(x, 18, 180, 20);

    panel.add(h);

    JLabel v = new JLabel(value);

    v.setFont(new Font("Segoe UI", Font.BOLD, 20));

    v.setForeground(new Color(0,102,204));

    v.setBounds(x, 42, 220, 30);

    panel.add(v);

    return v;
}



    // ─────────────────────────────────────────
    //  GENERATE REPORT
    // ─────────────────────────────────────────
    public void generateReport() {

        String view =
                viewByBox.getSelectedItem().toString();
    
        boolean courseWise =
                view.equals("Course Wise");
    
        reportScroll.setBounds(
                30,
                view.equals("Student Wise")
                        ? 450
                        : courseWise
                        ? 360
                        : 430,
                1120,
                view.equals("Student Wise")
                        ? 400
                        : courseWise
                        ? 340
                        : 270
        );
    
        updateTableColumns();
    
        switch (view) {
    
            case "Date Wise":
    
                analyticsPanel.setVisible(true);
                studentSummaryPanel.setVisible(false);
    
                loadDateWise(
                        getSpinnerDate(spinFrom),
                        getSpinnerDate(spinTo)
                );
    
                updateDateWiseAnalytics(
                        getSpinnerDate(spinFrom),
                        getSpinnerDate(spinTo)
                );
    
                break;
    
            case "Student Wise":
    
                analyticsPanel.setVisible(false);
                studentSummaryPanel.setVisible(true);
    
                loadStudentWise(
                        studentBox.getSelectedItem() != null
                                ? studentBox.getSelectedItem().toString()
                                : "",
                        getSpinnerDate(spinStudentFrom),
                        getSpinnerDate(spinStudentTo)
                );
    
                updateStudentSummary(
                        studentBox.getSelectedItem().toString()
                );
    
                break;
    
            case "Course Wise":
    
                analyticsPanel.setVisible(false);
                studentSummaryPanel.setVisible(false);
    
                loadCourseWise(
                        courseBox.getSelectedItem() != null
                                ? courseBox.getSelectedItem().toString()
                                : ""
                );
    
                updateCourseAnalysis();
    
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

    private void updateDateWiseAnalytics(
        String from,
        String to
) {

    try {

        // ==================================================
        // HIGHEST COLLECTION DAY
        // ==================================================

        pst = con.prepareStatement(

                "SELECT DATE(paid_at) d, " +
                "SUM(amount_paid) total " +
                "FROM fee_payments " +
                "WHERE DATE(paid_at) BETWEEN ? AND ? " +
                "GROUP BY DATE(paid_at) " +
                "ORDER BY total DESC " +
                "LIMIT 1"
        );

        pst.setString(1, from);

        pst.setString(2, to);

        rs = pst.executeQuery();

        if(rs.next()) {

            lblHighestDay.setText(
                    rs.getString("d") +
                    " | ₹ " +
                    String.format("%,.2f",
                            rs.getDouble("total"))
            );
        }

        // ==================================================
        // AVERAGE COLLECTION
        // ==================================================

        pst = con.prepareStatement(

                "SELECT AVG(day_total) avg_amt " +
                "FROM (" +
                "SELECT SUM(amount_paid) day_total " +
                "FROM fee_payments " +
                "WHERE DATE(paid_at) BETWEEN ? AND ? " +
                "GROUP BY DATE(paid_at)" +
                ") x"
        );

        pst.setString(1, from);

        pst.setString(2, to);

        rs = pst.executeQuery();

        if(rs.next()) {

            lblAverageDay.setText(
                    "₹ " +
                    String.format("%,.2f",
                            rs.getDouble("avg_amt"))
            );
        }

        // ==================================================
        // PENDING PAYMENTS
        // ==================================================

        pst = con.prepareStatement(

                "SELECT COUNT(*) cnt " +
                "FROM fee_payments " +
                "WHERE payment_status != 'Paid'"
        );

        rs = pst.executeQuery();

        if(rs.next()) {

            lblPendingPayments.setText(
                    String.valueOf(rs.getInt("cnt"))
            );
        }

        // ==================================================
        // TOP COURSE
        // ==================================================

        pst = con.prepareStatement(

                "SELECT c.course_name, " +
                "SUM(fp.amount_paid) total " +
                "FROM fee_payments fp " +
                "JOIN fee_payment_courses fpc " +
                "ON fpc.fee_payment_id = fp.id " +
                "JOIN courses c " +
                "ON c.id = fpc.course_id " +
                "GROUP BY c.course_name " +
                "ORDER BY total DESC " +
                "LIMIT 1"
        );

        rs = pst.executeQuery();

        if(rs.next()) {

            lblTopCourse.setText(
                    rs.getString("course_name")
            );
        }

    }
    catch(Exception ex) {

        ex.printStackTrace();
    }
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

    
    private void updateStudentSummary(
        String studentName
) {

    if(studentName.equals("-- All Students --")) {

        studentSummaryPanel.setVisible(false);

        return;
    }

    studentSummaryPanel.setVisible(true);

    try {

        pst = con.prepareStatement(

                "SELECT " +
                "SUM(fp.amount_paid) paid, " +
                "SUM(fp.discount_amt) discount, " +
                "MAX(fp.paid_at) last_payment " +
                "FROM fee_payments fp " +
                "JOIN students s " +
                "ON s.id = fp.student_id " +
                "WHERE s.name = ?"
        );

        pst.setString(1, studentName);

        rs = pst.executeQuery();

        if(rs.next()) {

            double paid =
                    rs.getDouble("paid");

            double discount =
                    rs.getDouble("discount");

            Timestamp ts =
                    rs.getTimestamp("last_payment");

            lblStudentTotalPaid.setText(
                    "₹ " +
                    String.format("%,.2f", paid)
            );

            lblStudentTotalDiscount.setText(
                    "₹ " +
                    String.format("%,.2f", discount)
            );

            lblStudentPending.setText(
                    "₹ 0"
            );

            lblStudentLastPayment.setText(
                    ts != null
                            ? ts.toLocalDateTime()
                            .format(DISP_FMT)
                            : "-"
            );
        }

    }
    catch(Exception ex) {

        ex.printStackTrace();
    }
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

    // ─────────────────────────────────────────
    //  COURSE ANALYSIS
    // ─────────────────────────────────────────
    private void updateCourseAnalysis() {

        String courseName = courseBox.getSelectedItem() != null
            ? courseBox.getSelectedItem().toString() : "";

        boolean allCourses = courseName.isEmpty()
            || courseName.equals("-- All Courses --");

        try {
            // ── Total institute revenue ──
            pst = con.prepareStatement(
                "SELECT COALESCE(SUM(amount_paid), 0) AS total " +
                "FROM fee_payments");
            rs = pst.executeQuery();
            double totalRevenue = 0.0;
            if (rs.next()) totalRevenue = rs.getDouble("total");
            lblTotalRevenue.setText(
                String.format("₹ %,.2f", totalRevenue));

            if (allCourses) {
                // show totals for all courses
                lblCourseStudents.setText(
                    String.valueOf(
                        (int) getCount(
                            "SELECT COUNT(DISTINCT student_id) " +
                            "FROM fee_payment_courses")));
                lblCourseRevenue.setText(
                    String.format("₹ %,.2f", totalRevenue));
                lblCoursePercent.setText("100.00%");

            // ==========================================================
            // AVERAGE REVENUE PER STUDENT
            // ==========================================================

            JLabel avgTitle = UIUtils.plain(
                    "Avg Revenue / Student:",
                    12
            );

            avgTitle.setBounds(900, 24, 180, 20);

            courseAnalysisPanel.add(avgTitle);

            lblAverageRevenuePerStudent =
                    UIUtils.bold("₹ 0", 16);

            lblAverageRevenuePerStudent.setForeground(
                    new Color(0,102,204)
            );

            lblAverageRevenuePerStudent.setBounds(
                    900,
                    44,
                    180,
                    24
            );

            courseAnalysisPanel.add(
                    lblAverageRevenuePerStudent
            );

                lblCoursePercentBar.setBounds(680, 78, 400, 18);
                return;
            }

            // ── Students enrolled in this course ──
            pst = con.prepareStatement(
                "SELECT COUNT(DISTINCT fpc.student_id) AS cnt " +
                "FROM fee_payment_courses fpc " +
                "JOIN courses c ON fpc.course_id = c.id " +
                "WHERE c.course_name = ?");
            pst.setString(1, courseName);
            rs = pst.executeQuery();
            int studentCount = 0;
            if (rs.next()) studentCount = rs.getInt("cnt");
            lblCourseStudents.setText(String.valueOf(studentCount));

            // ── Revenue from this course ──
            pst = con.prepareStatement(
                "SELECT COALESCE(SUM(fp.amount_paid), 0) AS rev " +
                "FROM fee_payments fp " +
                "JOIN fee_payment_courses fpc " +
                "  ON fpc.fee_payment_id = fp.id " +
                "JOIN courses c ON fpc.course_id = c.id " +
                "WHERE c.course_name = ?");
            pst.setString(1, courseName);
            rs = pst.executeQuery();
            double courseRevenue = 0.0;
            if (rs.next()) courseRevenue = rs.getDouble("rev");
            lblCourseRevenue.setText(
                String.format("₹ %,.2f", courseRevenue));

            // ── Percentage ──
            double percent = totalRevenue > 0
                ? (courseRevenue / totalRevenue) * 100.0 : 0.0;
            lblCoursePercent.setText(
                String.format("%.2f%%", percent));

                double avgRevenue =
                studentCount > 0
                        ? courseRevenue / studentCount
                        : 0;
        
        lblAverageRevenuePerStudent.setText(
                "₹ " +
                String.format("%,.2f", avgRevenue)
        );
        
            
            // ── Progress bar width (max 400px = 100%) ──
            int barWidth = (int) Math.round(percent * 4.0); // 4px per 1%
            barWidth = Math.min(barWidth, 400);
            lblCoursePercentBar.setBounds(680, 78, barWidth, 18);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private double getCount(String query) throws Exception {
        pst = con.prepareStatement(query);
        rs  = pst.executeQuery();
        return rs.next() ? rs.getDouble(1) : 0;
    }

}