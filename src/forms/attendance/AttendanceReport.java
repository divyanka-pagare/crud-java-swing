package src.forms.attendance;

import src.db.DBConnection;
import src.repositories.AttendanceRepository;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

public class AttendanceReport extends JFrame {

    // ===== FILTER CONTROLS =====
    JComboBox<String> viewByBox;
    JPanel            dateWisePanel, studentWisePanel, courseWisePanel;
    JComboBox<String> courseFilterBox, studentFilterBox;
    JSpinner          spinFrom, spinTo;

    // ===== SUMMARY STRIP =====
    JLabel lblTotalClasses, lblAvgAttendance,
           lblDefaulters,   lblPerfect;

    // ===== MAIN TABLE =====
    JTable            reportTable;
    DefaultTableModel reportModel;
    JScrollPane       reportScroll;

    // ===== DEFAULTERS ALERT PANEL =====
    JPanel  alertPanel;
    JLabel  lblAlertText;

    Connection             con;
    AttendanceRepository   repo;

    static final double  THRESHOLD    = 75.0;
    static final Color   CLR_GREEN    = new Color(40,  167, 69);
    static final Color   CLR_YELLOW   = new Color(200, 140, 0);
    static final Color   CLR_RED      = new Color(220, 53,  69);
    static final DateTimeFormatter DISP = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public AttendanceReport() {

        setTitle("Attendance Report");
        setSize(1200, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con  = DBConnection.getConnection();
        repo = new AttendanceRepository(con);

        JPanel main = UIUtils.mainPanel();

        // ── Title ──
        JLabel title = UIUtils.bold("Attendance Report", 28);
        title.setBounds(30, 12, 500, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  FILTER BAR
        // ─────────────────────────────────────────
        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        filterBar.setBounds(30, 62, 1120, 70);
        main.add(filterBar);

        filterBar.add(lbl("View By:", 15, 22));
        viewByBox = combo(new String[]{
            "Student Wise", "Course Wise", "Date Wise", "Defaulters"
        }, 85, 20, 160);
        filterBar.add(viewByBox);

        // ── Student Wise sub-panel ──
        studentWisePanel = new JPanel(null);
        studentWisePanel.setBackground(Color.WHITE);
        studentWisePanel.setBounds(260, 5, 700, 60);
        filterBar.add(studentWisePanel);

        studentWisePanel.add(lbl("Course:", 0, 20));
        courseFilterBox = combo(new String[]{}, 60, 18, 240);
        studentWisePanel.add(courseFilterBox);

        JButton btnGenStu = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 315, 18, 110, 32);
        studentWisePanel.add(btnGenStu);
        btnGenStu.addActionListener(e -> generateReport());

        // ── Course Wise sub-panel ──
        courseWisePanel = new JPanel(null);
        courseWisePanel.setBackground(Color.WHITE);
        courseWisePanel.setBounds(260, 5, 700, 60);
        courseWisePanel.setVisible(false);
        filterBar.add(courseWisePanel);

        courseWisePanel.add(lbl("Course:", 0, 20));
        JComboBox<String> courseWiseBox = new JComboBox<>();
        courseWiseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseWiseBox.setBounds(60, 18, 240, 32);
        courseWisePanel.add(courseWiseBox);

        JButton btnGenCrs = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 315, 18, 110, 32);
        courseWisePanel.add(btnGenCrs);

        // ── Date Wise sub-panel ──
        dateWisePanel = new JPanel(null);
        dateWisePanel.setBackground(Color.WHITE);
        dateWisePanel.setBounds(260, 5, 700, 60);
        dateWisePanel.setVisible(false);
        filterBar.add(dateWisePanel);

        dateWisePanel.add(lbl("Course:", 0, 20));
        JComboBox<String> dateCourseBx = new JComboBox<>();
        dateCourseBx.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateCourseBx.setBounds(60, 18, 200, 32);
        dateWisePanel.add(dateCourseBx);

        dateWisePanel.add(lbl("From:", 275, 20));
        spinFrom = dateSpin();
        spinFrom.setBounds(318, 18, 130, 32);
        dateWisePanel.add(spinFrom);

        dateWisePanel.add(lbl("To:", 462, 20));
        spinTo = dateSpin();
        spinTo.setBounds(484, 18, 130, 32);
        dateWisePanel.add(spinTo);

        JButton btnGenDate = UIUtils.colorButton("Generate",
            UIUtils.CLR_BLUE, 628, 18, 110, 32);
        dateWisePanel.add(btnGenDate);

        // ─────────────────────────────────────────
        //  SUMMARY STRIP
        // ─────────────────────────────────────────
        JPanel strip = new JPanel(null);
        strip.setBackground(new Color(245, 247, 250));
        strip.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        strip.setBounds(30, 145, 1120, 90);
        main.add(strip);

        lblTotalClasses  = statCard(strip, "Total Records",     "0",      new Color(0,102,204),  0);
        lblAvgAttendance = statCard(strip, "Avg Attendance %",  "0.00%",  CLR_GREEN,             1);
        lblDefaulters    = statCard(strip, "Defaulters (<75%)", "0",      CLR_RED,               2);
        lblPerfect       = statCard(strip, "Perfect (100%)",    "0",      new Color(23,162,184), 3);

        // ─────────────────────────────────────────
        //  ALERT PANEL
        // ─────────────────────────────────────────
        alertPanel = new JPanel(null);
        alertPanel.setBackground(new Color(255, 243, 205));
        alertPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7)));
        alertPanel.setBounds(30, 246, 1120, 36);
        alertPanel.setVisible(false);
        main.add(alertPanel);

        lblAlertText = new JLabel();
        lblAlertText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlertText.setForeground(new Color(130, 80, 0));
        lblAlertText.setBounds(12, 8, 1090, 20);
        alertPanel.add(lblAlertText);

        // ─────────────────────────────────────────
        //  REPORT TABLE
        // ─────────────────────────────────────────
        reportModel = new DefaultTableModel(
            new String[]{"#","Student","Course","Total","Present","Absent","Late","% Attendance","Status"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = TableUtils.createStyledTable(reportModel);
        reportTable.setRowHeight(36);
        reportTable.setFillsViewportHeight(true);

        // colour the % Attendance and Status columns
        reportTable.getColumnModel().getColumn(7).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    setHorizontalAlignment(JLabel.CENTER);
                    if (!sel && v != null) {
                        try {
                            double pct = Double.parseDouble(
                                v.toString().replace("%",""));
                            if      (pct >= 75) setForeground(CLR_GREEN);
                            else if (pct >= 60) setForeground(CLR_YELLOW);
                            else                setForeground(CLR_RED);
                        } catch (Exception ignored) {}
                    }
                    return this;
                }
            });

        reportTable.getColumnModel().getColumn(8).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                    setHorizontalAlignment(JLabel.CENTER);
                    if (!sel && v != null) {
                        String s = v.toString();
                        if      (s.equals("Eligible"))  setForeground(CLR_GREEN);
                        else if (s.equals("Defaulter")) setForeground(CLR_RED);
                        else                            setForeground(CLR_YELLOW);
                    }
                    return this;
                }
            });

        reportScroll = UIUtils.scrollPane(reportTable, 30, 292, 1120, 400);
        reportScroll.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        main.add(reportScroll);

        add(main);

        // ── load dropdowns ──
        loadCourseDropdowns(courseFilterBox, courseWiseBox, dateCourseBx);

        // spinners default
        resetSpin(spinFrom, -30);
        resetSpin(spinTo,     0);

        // ── view by listener ──
        viewByBox.addActionListener(e -> {
            String v = viewByBox.getSelectedItem().toString();
            studentWisePanel.setVisible(v.equals("Student Wise"));
            courseWisePanel .setVisible(v.equals("Course Wise"));
            dateWisePanel   .setVisible(v.equals("Date Wise"));
            generateReport();
        });

        btnGenStu .addActionListener(e -> generateReport());
        btnGenCrs .addActionListener(e -> generateReportCourseWise(courseWiseBox));
        btnGenDate.addActionListener(e -> generateReportDateWise(dateCourseBx));

        courseFilterBox.addActionListener(e -> generateReport());

        generateReport();
        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  GENERATE REPORT — Student Wise
    // ─────────────────────────────────────────
    public void generateReport() {
        String view = viewByBox.getSelectedItem().toString();

        if (view.equals("Course Wise") || view.equals("Date Wise")) return;

        reportModel.setRowCount(0);
        alertPanel.setVisible(false);

        if (view.equals("Defaulters")) {
            generateDefaulters();
            return;
        }

        // Student Wise
        reportModel.setColumnIdentifiers(new String[]{
            "#","Student","Course","Total","Present","Absent","Late","% Attendance","Status"});

        int    rowNum     = 1;
        int    defCount   = 0;
        int    perfCount  = 0;
        double totalPct   = 0;
        int    rowCount   = 0;

        String selCourse = courseFilterBox.getSelectedItem() != null
            ? courseFilterBox.getSelectedItem().toString() : "";
        boolean allCourses = selCourse.equals("All Courses");

        try {
            ResultSet rs = allCourses
                ? repo.getAllStudentsSummary()
                : repo.getStudentAttendanceSummary(getCourseId(selCourse));

            while (rs.next()) {
                double pct    = rs.getDouble("pct");
                String status = pct >= THRESHOLD ? "Eligible"
                              : pct >= 60        ? "At Risk"
                              :                    "Defaulter";

                reportModel.addRow(new Object[]{
                    rowNum++,
                    rs.getString("s.name") != null
                        ? rs.getString("s.name")
                        : rs.getString("name"),
                    allCourses ? rs.getString("course_name") : selCourse,
                    rs.getInt("total"),
                    rs.getInt("present_count"),
                    rs.getInt("absent_count"),
                    rs.getInt("late_count"),
                    String.format("%.2f%%", pct),
                    status
                });

                if (pct < THRESHOLD) defCount++;
                if (pct == 100)      perfCount++;
                totalPct += pct;
                rowCount++;
            }

            // update summary strip
            lblTotalClasses .setText(String.valueOf(rowCount));
            lblAvgAttendance.setText(rowCount > 0
                ? String.format("%.2f%%", totalPct / rowCount) : "0.00%");
            lblDefaulters   .setText(String.valueOf(defCount));
            lblPerfect      .setText(String.valueOf(perfCount));

            // show alert if defaulters exist
            if (defCount > 0) {
                alertPanel.setVisible(true);
                lblAlertText.setText("⚠  " + defCount +
                    " student(s) have attendance below " + (int)THRESHOLD +
                    "% and are at risk of being marked ineligible.");
            }

            TableUtils.resizeColumnWidth(reportTable);
            adjustTablePosition(false);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  GENERATE REPORT — Course Wise
    // ─────────────────────────────────────────
    private void generateReportCourseWise(JComboBox<String> courseBox) {
        reportModel.setRowCount(0);
        reportModel.setColumnIdentifiers(new String[]{
            "#","Date","Total Students","Present","Absent","Late","Attendance %"});
        alertPanel.setVisible(false);

        String cName = courseBox.getSelectedItem() != null
            ? courseBox.getSelectedItem().toString() : "";
        if (cName.isEmpty()) return;

        try {
            int cid = getCourseId(cName);
            ResultSet rs = repo.getDateWiseSummary(cid, "2000-01-01", "2099-12-31");
            int rowNum = 1;
            int total  = 0; double sumPct = 0;

            while (rs.next()) {
                Date d   = rs.getDate("attendance_date");
                int  tot = rs.getInt("total");
                int  pre = rs.getInt("present_count");
                double pct = tot > 0 ? (pre * 100.0 / tot) : 0;

                reportModel.addRow(new Object[]{
                    rowNum++,
                    d != null ? d.toLocalDate().format(DISP) : "—",
                    tot,
                    pre,
                    rs.getInt("absent_count"),
                    rs.getInt("late_count"),
                    String.format("%.2f%%", pct)
                });
                total++; sumPct += pct;
            }

            lblTotalClasses .setText(String.valueOf(total));
            lblAvgAttendance.setText(total>0
                ? String.format("%.2f%%", sumPct/total) : "0.00%");
            lblDefaulters   .setText("—");
            lblPerfect      .setText("—");

            TableUtils.resizeColumnWidth(reportTable);
            adjustTablePosition(false);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  GENERATE REPORT — Date Wise
    // ─────────────────────────────────────────
    private void generateReportDateWise(JComboBox<String> courseBox) {
        reportModel.setRowCount(0);
        reportModel.setColumnIdentifiers(new String[]{
            "#","Student","Status","Remarks"});
        alertPanel.setVisible(false);

        String cName = courseBox.getSelectedItem() != null
            ? courseBox.getSelectedItem().toString() : "";
        if (cName.isEmpty()) return;

        String from = spinDate(spinFrom);
        String to   = spinDate(spinTo);

        try {
            int cid = getCourseId(cName);
            PreparedStatement ps = con.prepareStatement(
                "SELECT s.name, a.attendance_date, a.status, a.remarks " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id=s.id " +
                "WHERE a.course_id=? AND a.attendance_date BETWEEN ? AND ? " +
                "ORDER BY a.attendance_date DESC, s.name");
            ps.setInt(1, cid);
            ps.setString(2, from);
            ps.setString(3, to);
            ResultSet rs = ps.executeQuery();

            int rowNum=1, p=0, a=0, l=0;
            while (rs.next()) {
                String st = rs.getString("a.status");
                if ("Present".equals(st)) p++;
                else if ("Absent".equals(st)) a++;
                else l++;
                reportModel.addRow(new Object[]{
                    rowNum++,
                    rs.getString("s.name"),
                    st,
                    rs.getString("a.remarks")
                });
            }

            lblTotalClasses .setText(String.valueOf(rowNum-1));
            lblAvgAttendance.setText(rowNum>1
                ? String.format("%.2f%%",(p*100.0/(rowNum-1))) : "0%");
            lblDefaulters   .setText(String.valueOf(a));
            lblPerfect      .setText(String.valueOf(p));

            TableUtils.resizeColumnWidth(reportTable);
            adjustTablePosition(false);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  DEFAULTERS
    // ─────────────────────────────────────────
    private void generateDefaulters() {
        reportModel.setColumnIdentifiers(new String[]{
            "#","Student","Course","Total Classes",
            "Present","% Attendance","Classes Needed for 75%"});

        try {
            ResultSet rs = repo.getDefaulters(THRESHOLD);
            int rowNum = 1;

            while (rs.next()) {
                int    total   = rs.getInt("total");
                int    present = rs.getInt("present_count");
                double pct     = rs.getDouble("pct");

                // classes needed: x = (0.75*(total+x) - present) => x = (0.75*total - present) / 0.25
                int needed = 0;
                if (pct < THRESHOLD) {
                    double n = (THRESHOLD/100.0 * total - present) / (1 - THRESHOLD/100.0);
                    needed = (int) Math.ceil(n);
                }

                reportModel.addRow(new Object[]{
                    rowNum++,
                    rs.getString("s.name"),
                    rs.getString("c.course_name"),
                    total,
                    present,
                    String.format("%.2f%%", pct),
                    needed > 0 ? needed + " more classes" : "Eligible"
                });
            }

            lblTotalClasses .setText(String.valueOf(reportModel.getRowCount()));
            lblAvgAttendance.setText("< " + (int)THRESHOLD + "%");
            lblDefaulters   .setText(String.valueOf(reportModel.getRowCount()));
            lblPerfect      .setText("0");

            if (reportModel.getRowCount() > 0) {
                alertPanel.setVisible(true);
                lblAlertText.setText("⚠  " + reportModel.getRowCount() +
                    " student(s) are below 75% attendance and are marked as Defaulters.");
            }

            TableUtils.resizeColumnWidth(reportTable);
            adjustTablePosition(false);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  TABLE POSITION
    // ─────────────────────────────────────────
    private void adjustTablePosition(boolean alertVisible) {
        int tableY = alertVisible ? 292 : 258;
        int tableH = getHeight() - tableY - 50;
        reportScroll.setBounds(30, tableY, 1120, Math.max(tableH, 200));
        revalidate(); repaint();
    }

    // ─────────────────────────────────────────
    //  LOAD COURSE DROPDOWNS
    // ─────────────────────────────────────────
    private void loadCourseDropdowns(JComboBox<String>... boxes) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT course_name FROM courses ORDER BY course_name");
            ResultSet rs = ps.executeQuery();
            List<String> names = new ArrayList<>();
            names.add("All Courses");
            while (rs.next()) names.add(rs.getString("course_name"));
            for (JComboBox<String> box : boxes) {
                box.removeAllItems();
                for (String n : names) box.addItem(n);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private int getCourseId(String courseName) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "SELECT id FROM courses WHERE course_name=?");
        ps.setString(1, courseName);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private JSpinner dateSpin() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "dd-MM-yyyy"));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return s;
    }

    private void resetSpin(JSpinner sp, int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        sp.setValue(c.getTime());
    }

    private String spinDate(JSpinner sp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format((java.util.Date) sp.getValue());
    }

    private JLabel lbl(String text, int x, int y) {
        JLabel l = UIUtils.plain(text, 13);
        l.setBounds(x, y, 80, 26);
        return l;
    }

    private JComboBox<String> combo(String[] items, int x, int y, int w) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBounds(x, y, w, 32);
        return c;
    }

    private JLabel statCard(JPanel parent, String head,
                             String val, Color accent, int index) {
        int cardW = 275, gap = 10;
        int x = gap + index * (cardW + gap);

        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accent);
                g.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        card.setBounds(x, 10, cardW, 70);
        parent.add(card);

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setBounds(14, 12, 7, 7);
        card.add(dot);

        JLabel lHead = new JLabel(head.toUpperCase());
        lHead.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lHead.setForeground(new Color(130, 130, 140));
        lHead.setBounds(28, 10, cardW - 32, 14);
        card.add(lHead);

        JLabel lVal = new JLabel(val);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lVal.setForeground(new Color(30, 30, 40));
        lVal.setBounds(14, 28, cardW - 18, 30);
        card.add(lVal);

        return lVal;
    }

    // suppress unchecked for varargs
    @SuppressWarnings("unchecked")
    private void loadCourseDropdowns(JComboBox<String> a,
                                      JComboBox<String> b,
                                      JComboBox<String> c) {
        loadCourseDropdowns(new JComboBox[]{a, b, c});
    }
}