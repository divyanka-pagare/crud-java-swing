package src.forms.report;

import src.db.DBConnection;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class FeesCollectionReport extends JFrame {

    // ===== FILTER CONTROLS =====
    JComboBox<String> viewByBox;
    JPanel      dateWisePanel;
    JSpinner    spinFrom, spinTo;
    JPanel            studentWisePanel;
    JComboBox<String> studentBox;
    JSpinner          spinStudentFrom, spinStudentTo;
    JPanel            courseWisePanel;
    JComboBox<String> courseBox;
    JScrollPane reportScroll;

    // ===== ANALYTICS PANELS =====
    JPanel analyticsPanel;
    JLabel lblHighestDay, lblAverageDay, lblPendingPayments, lblTopCourse;

    JPanel studentSummaryPanel;
    JLabel lblStudentTotalPaid, lblStudentTotalDiscount,
           lblStudentPending,   lblStudentLastPayment;

    JPanel courseAnalysisPanel;
    JLabel lblCourseStudents, lblCourseRevenue,
           lblTotalRevenue,   lblCoursePercent,
           lblAvgRevenue,     lblCoursePercentBar;

    // ===== SUMMARY CARDS =====
    JLabel lblTotalCollection, lblTotalStudents,
           lblTotalCourses,    lblTotalPayments;

    // ===== TABLE =====
    JTable            reportTable;
    DefaultTableModel reportModel;

    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

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
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(210,215,220)));
        filterBar.setBounds(30, 62, 1120, 70);
        main.add(filterBar);

        JLabel lView = UIUtils.plain("View By:", 13);
        lView.setBounds(15, 22, 70, 26);
        filterBar.add(lView);

        viewByBox = new JComboBox<>(new String[]{"Date Wise","Student Wise","Course Wise"});
        viewByBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        viewByBox.setBounds(88, 20, 140, 32);
        filterBar.add(viewByBox);

        // ── Date Wise Panel ──
        dateWisePanel = new JPanel(null);
        dateWisePanel.setBackground(Color.WHITE);
        dateWisePanel.setBounds(245, 5, 700, 60);
        filterBar.add(dateWisePanel);

        UIUtils.plain("From:", 13).setBounds(0, 18, 45, 26);
        dateWisePanel.add(label("From:", 0, 18));
        spinFrom = dateSpinner();
        spinFrom.setBounds(48, 16, 145, 30);
        dateWisePanel.add(spinFrom);
        dateWisePanel.add(label("To:", 205, 18));
        spinTo = dateSpinner();
        spinTo.setBounds(238, 16, 145, 30);
        dateWisePanel.add(spinTo);

        JButton btnGen1 = UIUtils.colorButton("Generate", UIUtils.CLR_BLUE, 398, 16, 110, 32);
        JButton btnClr1 = UIUtils.colorButton("Clear",    UIUtils.CLR_GRAY, 518, 16,  80, 32);
        dateWisePanel.add(btnGen1);
        dateWisePanel.add(btnClr1);
        btnGen1.addActionListener(e -> generateReport());
        btnClr1.addActionListener(e -> { resetSpinner(spinFrom,-30); resetSpinner(spinTo,0); generateReport(); });

        // ── Student Wise Panel ──
        studentWisePanel = new JPanel(null);
        studentWisePanel.setBackground(Color.WHITE);
        studentWisePanel.setBounds(245, 5, 840, 60);
        studentWisePanel.setVisible(false);
        filterBar.add(studentWisePanel);

        studentWisePanel.add(label("Student:", 0, 18));
        studentBox = new JComboBox<>();
        studentBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentBox.setBounds(70, 16, 200, 32);
        studentWisePanel.add(studentBox);
        studentWisePanel.add(label("From:", 282, 18));
        spinStudentFrom = dateSpinner();
        spinStudentFrom.setBounds(328, 16, 145, 30);
        studentWisePanel.add(spinStudentFrom);
        studentWisePanel.add(label("To:", 485, 18));
        spinStudentTo = dateSpinner();
        spinStudentTo.setBounds(518, 16, 145, 30);
        studentWisePanel.add(spinStudentTo);
        JButton btnGen2 = UIUtils.colorButton("Generate", UIUtils.CLR_BLUE, 675, 16, 110, 32);
        studentWisePanel.add(btnGen2);
        btnGen2.addActionListener(e -> generateReport());

        // ── Course Wise Panel ──
        courseWisePanel = new JPanel(null);
        courseWisePanel.setBackground(Color.WHITE);
        courseWisePanel.setBounds(245, 5, 500, 60);
        courseWisePanel.setVisible(false);
        filterBar.add(courseWisePanel);

        courseWisePanel.add(label("Course:", 0, 18));
        courseBox = new JComboBox<>();
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseBox.setBounds(66, 16, 260, 32);
        courseWisePanel.add(courseBox);
        JButton btnGen3 = UIUtils.colorButton("Generate", UIUtils.CLR_BLUE, 338, 16, 110, 32);
        courseWisePanel.add(btnGen3);
        btnGen3.addActionListener(e -> generateReport());

        // ─────────────────────────────────────────
        //  4 SUMMARY CARDS (always visible)
        // ─────────────────────────────────────────
        lblTotalCollection = summaryCard(main,"Total Collection","₹ 0.00", new Color(0,102,204),  30,145);
        lblTotalStudents   = summaryCard(main,"Students Paid",   "0",      new Color(40,167,69),  305,145);
        lblTotalCourses    = summaryCard(main,"Courses Enrolled","0",      new Color(153,0,153),  580,145);
        lblTotalPayments   = summaryCard(main,"Total Transactions","0",    new Color(220,53,69),  855,145);

        // ─────────────────────────────────────────
        //  DATE WISE — analytics strip
        // ─────────────────────────────────────────
        analyticsPanel = strip(main, 30, 240, 1120, 130);

        lblHighestDay      = statCard(analyticsPanel,"Highest Collection Day","—",         new Color(0,102,204), 0);
        lblAverageDay      = statCard(analyticsPanel,"Average Daily Collection","₹ 0",     new Color(40,167,69), 1);
        lblPendingPayments = statCard(analyticsPanel,"Pending Payments","0",               new Color(220,53,69), 2);
        lblTopCourse       = statCard(analyticsPanel,"Top Revenue Course","—",             new Color(153,0,153), 3);

        // ─────────────────────────────────────────
        //  STUDENT WISE — analytics strip
        // ─────────────────────────────────────────
        studentSummaryPanel = strip(main, 30, 240, 1120, 100);
        studentSummaryPanel.setVisible(false);

        lblStudentTotalPaid     = statCard(studentSummaryPanel,"Total Fees Paid","₹ 0",      new Color(0,102,204), 0);
        lblStudentTotalDiscount = statCard(studentSummaryPanel,"Total Discount", "₹ 0",      new Color(40,167,69), 1);
        lblStudentPending       = statCard(studentSummaryPanel,"Pending Amount", "₹ 0",      new Color(220,53,69), 2);
        lblStudentLastPayment   = statCard(studentSummaryPanel,"Last Payment Date","—",       new Color(153,0,153), 3);

        // ─────────────────────────────────────────
        //  COURSE WISE — analytics strip (5 items)
        // ─────────────────────────────────────────
        courseAnalysisPanel = strip(main, 30, 240, 1120, 130);
        courseAnalysisPanel.setVisible(false);
        lblCourseStudents = statCard(courseAnalysisPanel,"Students Enrolled",        "0",      new Color(0,102,204),  0);

        lblCourseRevenue  = statCard(courseAnalysisPanel,"Total Transactions",       "0",      new Color(40,167,69),  1);
        
        lblTotalRevenue   = statCard(courseAnalysisPanel,"Top Revenue Course",       "—",      new Color(153,0,153),  2);
        
        lblCoursePercent  = statCard(courseAnalysisPanel,"Contribution %",           "0.00%",  new Color(220,53,69),  3);
        
        lblAvgRevenue     = statCard(courseAnalysisPanel,"Avg Revenue / Student",    "₹ 0.00", new Color(23,162,184), 4);
        // ─────────────────────────────────────────
        //  REPORT TABLE
        // ─────────────────────────────────────────
        reportModel = new DefaultTableModel(
            new String[]{"#","Date","Student","Courses","Mode","Discount","Amount Paid","Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = TableUtils.createStyledTable(reportModel);
        reportTable.setFillsViewportHeight(true);
        reportTable.setRowHeight(38);

        // colour Paid/Pending in Status column
        reportTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    if (val.equalsIgnoreCase("Paid"))    comp.setForeground(new Color(40,167,69));
                    else if (val.equalsIgnoreCase("Pending")) comp.setForeground(new Color(220,53,69));
                    else comp.setForeground(Color.BLACK);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return comp;
            }
        });

        reportScroll = UIUtils.scrollPane(reportTable, 30, 350, 1120, 400);
        reportScroll.getViewport().setBackground(Color.WHITE);
        reportScroll.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        main.add(reportScroll);

        add(main);

        // ── Load dropdowns ──
        loadStudentDropdown();
        loadCourseDropdown();

        resetSpinner(spinFrom, -30);
        resetSpinner(spinTo,     0);
        setStudentDefaultDates();

        // ── Listeners ──
        viewByBox.addActionListener(e -> {
            String v = viewByBox.getSelectedItem().toString();
            dateWisePanel   .setVisible(v.equals("Date Wise"));
            studentWisePanel.setVisible(v.equals("Student Wise"));
            courseWisePanel .setVisible(v.equals("Course Wise"));
            updateTableColumns();
            generateReport();
        });

        studentBox.addActionListener(e -> generateReport());
        courseBox .addActionListener(e -> generateReport());

        generateReport();
        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  GENERATE REPORT
    // ─────────────────────────────────────────
    public void generateReport() {

        String view = viewByBox.getSelectedItem().toString();
        updateTableColumns();

        analyticsPanel     .setVisible(false);
        studentSummaryPanel.setVisible(false);
        courseAnalysisPanel.setVisible(false);

        int tableY;

        switch (view) {

            case "Date Wise":
                analyticsPanel.setVisible(true);
                analyticsPanel.setBounds(30, 240, 1120, 110);
                tableY = 380;
                loadDateWise(spinDate(spinFrom), spinDate(spinTo));
                updateDateAnalytics(spinDate(spinFrom), spinDate(spinTo));
                break;

            case "Student Wise":
                String sName = studentBox.getSelectedItem() != null
                    ? studentBox.getSelectedItem().toString() : "";
                boolean show = !sName.equals("-- All Students --");
                studentSummaryPanel.setVisible(show);
                studentSummaryPanel.setBounds(30, 240, 1120, 110);
                tableY = show ? 380 : 240;
                loadStudentWise(sName, spinDate(spinStudentFrom), spinDate(spinStudentTo));
                if (show) updateStudentSummary(sName);
                break;

            case "Course Wise":
                courseAnalysisPanel.setVisible(true);
                courseAnalysisPanel.setBounds(30, 240, 1120, 110);
                tableY = 380;
                String cName = courseBox.getSelectedItem() != null
                    ? courseBox.getSelectedItem().toString() : "";
                loadCourseWise(cName);
                updateCourseAnalysis(cName);
                break;

            default:
                tableY = 240;
                break;
        }

        int tableH = getHeight() - tableY - 50;
        reportScroll.setBounds(30, tableY, 1120, Math.max(tableH, 200));

        updateSummaryCards();
        TableUtils.resizeColumnWidth(reportTable);
        revalidate();
        repaint();
    }

    // ─────────────────────────────────────────
    //  UPDATE COLUMNS
    // ─────────────────────────────────────────
    private void updateTableColumns() {
        reportModel.setRowCount(0);
        switch (viewByBox.getSelectedItem().toString()) {
            case "Date Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Date","Student","Courses","Mode","Discount","Amount Paid","Status"});
                break;
            case "Student Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Payment Date","Courses Paid","Mode","Discount","Amount Paid","Status"});
                break;
            case "Course Wise":
                reportModel.setColumnIdentifiers(new String[]{
                    "#","Student","Payment Date","Mode","Amount Paid","Status"});
                break;
        }
    }

    // ─────────────────────────────────────────
    //  DATE WISE DATA
    // ─────────────────────────────────────────
    private void loadDateWise(String from, String to) {
        try {
            pst = con.prepareStatement(
                "SELECT fp.paid_at, s.name, " +
                "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                "fp.payment_mode, fp.discount_amt, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id " +
                "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
                "LEFT JOIN courses c ON fpc.course_id = c.id " +
                "WHERE DATE(fp.paid_at) BETWEEN ? AND ? " +
                "GROUP BY fp.id ORDER BY fp.paid_at DESC");
            pst.setString(1, from); pst.setString(2, to);
            rs = pst.executeQuery();
            int i = 1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String dt = ts!=null ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                double d  = rs.getDouble("discount_amt");
                reportModel.addRow(new Object[]{
                    i++, dt, rs.getString("s.name"),
                    rs.getString("courses")!=null ? rs.getString("courses") : "—",
                    rs.getString("payment_mode"),
                    d>0 ? String.format("- ₹ %,.2f",d) : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateDateAnalytics(String from, String to) {
        try {
            // highest day
            pst = con.prepareStatement(
                "SELECT DATE(paid_at) d, SUM(amount_paid) total FROM fee_payments " +
                "WHERE DATE(paid_at) BETWEEN ? AND ? GROUP BY DATE(paid_at) ORDER BY total DESC LIMIT 1");
            pst.setString(1,from); pst.setString(2,to); rs=pst.executeQuery();
            lblHighestDay.setText(rs.next()
                ? rs.getString("d")+" | ₹ "+String.format("%,.2f",rs.getDouble("total")) : "—");

            // average
            pst = con.prepareStatement(
                "SELECT AVG(dt) avg FROM (SELECT SUM(amount_paid) dt FROM fee_payments " +
                "WHERE DATE(paid_at) BETWEEN ? AND ? GROUP BY DATE(paid_at)) x");
            pst.setString(1,from); pst.setString(2,to); rs=pst.executeQuery();
            lblAverageDay.setText(rs.next()
                ? "₹ "+String.format("%,.2f",rs.getDouble("avg")) : "₹ 0");

            // pending
            pst = con.prepareStatement(
                "SELECT COUNT(*) cnt FROM fee_payments WHERE payment_status != 'Paid'");
            rs=pst.executeQuery();
            lblPendingPayments.setText(rs.next() ? String.valueOf(rs.getInt("cnt")) : "0");

            // top course
            pst = con.prepareStatement(
                "SELECT c.course_name, SUM(fp.amount_paid) total " +
                "FROM fee_payments fp JOIN fee_payment_courses fpc ON fpc.fee_payment_id=fp.id " +
                "JOIN courses c ON c.id=fpc.course_id GROUP BY c.course_name ORDER BY total DESC LIMIT 1");
            rs=pst.executeQuery();
            lblTopCourse.setText(rs.next() ? rs.getString("course_name") : "—");

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  STUDENT WISE DATA
    // ─────────────────────────────────────────
    private void loadStudentWise(String sName, String from, String to) {
        try {
            boolean all = sName.isEmpty() || sName.equals("-- All Students --");
            StringBuilder q = new StringBuilder(
                "SELECT fp.paid_at, " +
                "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
                "fp.payment_mode, fp.discount_amt, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp JOIN students s ON fp.student_id=s.id " +
                "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id=fp.id " +
                "LEFT JOIN courses c ON fpc.course_id=c.id " +
                "WHERE DATE(fp.paid_at) BETWEEN ? AND ? ");
            if (!all) q.append("AND s.name=? ");
            q.append("GROUP BY fp.id ORDER BY fp.paid_at DESC");

            pst = con.prepareStatement(q.toString());
            pst.setString(1,from); pst.setString(2,to);
            if (!all) pst.setString(3,sName);
            rs = pst.executeQuery();
            int i=1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String dt = ts!=null ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                double d  = rs.getDouble("discount_amt");
                reportModel.addRow(new Object[]{
                    i++, dt,
                    rs.getString("courses")!=null ? rs.getString("courses") : "—",
                    rs.getString("payment_mode"),
                    d>0 ? String.format("- ₹ %,.2f",d) : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateStudentSummary(String sName) {
        try {
            pst = con.prepareStatement(
                "SELECT SUM(fp.amount_paid) paid, SUM(fp.discount_amt) disc, MAX(fp.paid_at) last " +
                "FROM fee_payments fp JOIN students s ON s.id=fp.student_id WHERE s.name=?");
            pst.setString(1,sName); rs=pst.executeQuery();
            if (rs.next()) {
                lblStudentTotalPaid    .setText("₹ "+String.format("%,.2f",rs.getDouble("paid")));
                lblStudentTotalDiscount.setText("₹ "+String.format("%,.2f",rs.getDouble("disc")));
                lblStudentPending      .setText("₹ 0");
                Timestamp ts = rs.getTimestamp("last");
                lblStudentLastPayment  .setText(ts!=null
                    ? ts.toLocalDateTime().format(DISP_FMT) : "—");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  COURSE WISE DATA
    // ─────────────────────────────────────────
    private void loadCourseWise(String cName) {
        try {
            boolean all = cName.isEmpty() || cName.equals("-- All Courses --");
            StringBuilder q = new StringBuilder(
                "SELECT s.name, fp.paid_at, fp.payment_mode, fp.amount_paid, fp.payment_status " +
                "FROM fee_payments fp JOIN students s ON fp.student_id=s.id " +
                "JOIN fee_payment_courses fpc ON fpc.fee_payment_id=fp.id " +
                "JOIN courses c ON fpc.course_id=c.id WHERE 1=1 ");
            if (!all) q.append("AND c.course_name=? ");
            q.append("ORDER BY fp.paid_at DESC");

            pst = con.prepareStatement(q.toString());
            if (!all) pst.setString(1,cName);
            rs = pst.executeQuery();
            int i=1;
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String dt = ts!=null ? ts.toLocalDateTime().format(DISP_FMT) : "—";
                reportModel.addRow(new Object[]{
                    i++, rs.getString("s.name"), dt,
                    rs.getString("payment_mode"),
                    String.format("₹ %,.2f",rs.getDouble("amount_paid")),
                    rs.getString("payment_status")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateCourseAnalysis(String cName) {
        boolean all = cName.isEmpty() || cName.equals("-- All Courses --");
        try {
            // total institute revenue
            pst = con.prepareStatement(
                "SELECT COALESCE(SUM(amount_paid),0) total FROM fee_payments");
            rs=pst.executeQuery();
            double totalRev = rs.next() ? rs.getDouble("total") : 0;
            

            if (all) {

                // Total Students
                pst = con.prepareStatement(
                    "SELECT COUNT(DISTINCT student_id) cnt FROM fee_payments");
                rs = pst.executeQuery();
            
                int totalStudents = 0;
            
                if (rs.next()) {
                    totalStudents = rs.getInt("cnt");
                }
            
                lblCourseStudents.setText(String.valueOf(totalStudents));
            
                pst = con.prepareStatement(
                    "SELECT COUNT(*) cnt FROM fee_payments");
                
                rs = pst.executeQuery();
                
                if(rs.next()) {
                    lblCourseRevenue.setText(
                        String.valueOf(rs.getInt("cnt")));
                }

                // Total Transactions
                pst = con.prepareStatement(
                    "SELECT COUNT(*) cnt FROM fee_payments");
                rs = pst.executeQuery();
            
                int transactions = 0;
            
                if (rs.next()) {
                    transactions = rs.getInt("cnt");
                }

                pst = con.prepareStatement(
                    "SELECT c.course_name, SUM(fp.amount_paid) revenue " +
                    "FROM fee_payments fp " +
                    "JOIN fee_payment_courses fpc ON fp.id=fpc.fee_payment_id " +
                    "JOIN courses c ON c.id=fpc.course_id " +
                    "GROUP BY c.course_name " +
                    "ORDER BY revenue DESC " +
                    "LIMIT 1");
                
                rs = pst.executeQuery();
                
                if(rs.next()) {
                    lblTotalRevenue.setText(
                        rs.getString("course_name"));
                }

                // Average Revenue Per Student
                double avgRevenue =
                    totalStudents > 0
                    ? totalRev / totalStudents
                    : 0;
            
                lblAvgRevenue.setText(
                    "₹ " + String.format("%,.2f", avgRevenue));
            
                // Top Revenue Course
                pst = con.prepareStatement(
                    "SELECT c.course_name, SUM(fp.amount_paid) revenue " +
                    "FROM fee_payments fp " +
                    "JOIN fee_payment_courses fpc ON fp.id=fpc.fee_payment_id " +
                    "JOIN courses c ON c.id=fpc.course_id " +
                    "GROUP BY c.course_name " +
                    "ORDER BY revenue DESC " +
                    "LIMIT 1");
            
                rs = pst.executeQuery();
            
                if (rs.next()) {
                    lblCoursePercent.setText(rs.getString("100.00%"));
                } else {
                    lblCoursePercent.setText("—");
                }
            } else {
                // specific course students
                pst = con.prepareStatement(
                    "SELECT COUNT(DISTINCT fpc.student_id) cnt FROM fee_payment_courses fpc " +
                    "JOIN courses c ON fpc.course_id=c.id WHERE c.course_name=?");
                pst.setString(1,cName); rs=pst.executeQuery();
                int stuCount = rs.next() ? rs.getInt("cnt") : 0;
                lblCourseStudents.setText(String.valueOf(stuCount));

                // course revenue
                pst = con.prepareStatement(
                    "SELECT COALESCE(SUM(fp.amount_paid),0) rev FROM fee_payments fp " +
                    "JOIN fee_payment_courses fpc ON fpc.fee_payment_id=fp.id " +
                    "JOIN courses c ON fpc.course_id=c.id WHERE c.course_name=?");
                pst.setString(1,cName); rs=pst.executeQuery();
                double cRev = rs.next() ? rs.getDouble("rev") : 0;
                
                pst = con.prepareStatement(
                    "SELECT COUNT(DISTINCT fp.id) cnt " +
                    "FROM fee_payments fp" +
                    "JOIN fee_payment_courses fpc ON fp.id=fpc.fee_payment_id " +
                    "JOIN courses c ON c.id=fpc.course_id " +
                    "WHERE c.course_name=?");
                
                pst.setString(1,cName);
                
                rs = pst.executeQuery();
                
                if(rs.next()) {
                    lblCourseRevenue.setText(
                        String.valueOf(rs.getInt("cnt")));
                }

                double pct = totalRev>0 ? (cRev/totalRev)*100.0 : 0;
                lblCoursePercent.setText(String.format("%.2f%%", pct));

                pst = con.prepareStatement(
                    "SELECT c.course_name, SUM(fp.amount_paid) revenue " +
                    "FROM fee_payments fp " +
                    "JOIN fee_payment_courses fpc ON fp.id=fpc.fee_payment_id " +
                    "JOIN courses c ON c.id=fpc.course_id " +
                    "GROUP BY c.course_name " +
                    "ORDER BY revenue DESC " +
                    "LIMIT 1");
                
                rs = pst.executeQuery();
                
                if(rs.next()) {
                    lblTotalRevenue.setText(
                        rs.getString("course_name"));
                }

                double avg = stuCount>0 ? cRev/stuCount : 0;
                lblAvgRevenue.setText("₹ "+String.format("%,.2f", avg));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  SUMMARY CARDS (top 4)
    // ─────────────────────────────────────────
    private void updateSummaryCards() {
        try {
            pst = con.prepareStatement(
                "SELECT " +
                "(SELECT COALESCE(SUM(amount_paid),0) FROM fee_payments) total, " +
                "(SELECT COUNT(DISTINCT student_id) FROM fee_payments) students, " +
                "(SELECT COUNT(*) FROM courses) courses, " +
                "(SELECT COUNT(*) FROM fee_payments) transactions"
            );
            rs=pst.executeQuery();
            if (rs.next()) {
                lblTotalCollection.setText(String.format("₹ %,.2f",rs.getDouble("total")));
                lblTotalStudents  .setText(String.valueOf(rs.getInt("students")));
                lblTotalCourses   .setText(String.valueOf(rs.getInt("courses")));
                lblTotalPayments  .setText(String.valueOf(rs.getInt("transactions")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
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
                "JOIN students s ON fp.student_id=s.id ORDER BY s.name");
            rs=pst.executeQuery();
            while (rs.next()) studentBox.addItem(rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadCourseDropdown() {
        courseBox.removeAllItems();
        courseBox.addItem("-- All Courses --");
        try {
            pst = con.prepareStatement("SELECT course_name FROM courses ORDER BY course_name");
            rs=pst.executeQuery();
            while (rs.next()) courseBox.addItem(rs.getString("course_name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private JSpinner dateSpinner() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s,"dd-MM-yyyy"));
        s.setFont(new Font("Segoe UI",Font.PLAIN,13));
        return s;
    }

    private void resetSpinner(JSpinner sp, int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        sp.setValue(c.getTime());
    }

    private void setStudentDefaultDates() {
        Calendar to=Calendar.getInstance(), from=Calendar.getInstance();
        from.add(Calendar.YEAR,-1);
        spinStudentTo.setValue(to.getTime());
        spinStudentFrom.setValue(from.getTime());
    }

    private String spinDate(JSpinner sp) {
        java.util.Date d=(java.util.Date)sp.getValue();
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private JLabel label(String text, int x, int y) {
        JLabel l = UIUtils.plain(text, 13);
        l.setBounds(x, y, 80, 26);
        return l;
    }

    // ── coloured summary card (top row) ──
    private JLabel summaryCard(JPanel p, String head, String val, Color bg, int x, int y) {
        JPanel card = new JPanel(null);
        card.setBackground(bg);
        card.setBounds(x, y, 245, 80);
        p.add(card);
        JLabel h = new JLabel(head);
        h.setFont(new Font("Segoe UI",Font.PLAIN,12));
        h.setForeground(new Color(220,220,220));
        h.setBounds(14,10,220,20);
        card.add(h);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI",Font.BOLD,20));
        v.setForeground(Color.WHITE);
        v.setBounds(14,34,220,32);
        card.add(v);
        return v;
    }

    // ── grey strip container ──
    private JPanel strip(JPanel parent, int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(245,247,250));
        p.setBorder(BorderFactory.createLineBorder(new Color(210,215,220)));
        p.setBounds(x,y,w,h);
        parent.add(p);
        return p;
    }

    // ── white card inside strip ──
    // index 0-3 = 4 equal cards; index 4 = 5th card (narrower)
    private JLabel statCard(JPanel parent, String head, String val, Color accent, int index) {
        int total  = parent.getPreferredSize().width > 0
            ? parent.getPreferredSize().width : 1120;
        int count  = 5; // always 5 slots wide; unused slots stay empty
        int cardW  = 270;
        int gap    = 8;
        int x      = gap + index*(cardW+gap);

        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accent);
                g.fillRect(0,0,5,getHeight());
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        card.setBounds(x, 10, cardW, 100);
        parent.add(card);

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setBounds(14,14,7,7);
        card.add(dot);

        JLabel lHead = new JLabel(head.toUpperCase());
        lHead.setFont(new Font("Segoe UI",Font.PLAIN,10));
        lHead.setForeground(new Color(130,130,140));
        lHead.setBounds(28,11,cardW-32,16);
        card.add(lHead);

        JLabel lVal = new JLabel(val);
        lVal.setFont(new Font("Segoe UI",Font.BOLD,18));
        lVal.setForeground(new Color(30,30,40));
        lVal.setBounds(14,34,cardW-18,50);
        card.add(lVal);

        return lVal;
    }
}