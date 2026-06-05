package src.forms.attendance;

import src.db.DBConnection;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class TodayAttendanceLog extends JFrame {

    JComboBox<String> courseBox;
    JButton           btnRefresh, btnBack;

    // ===== RING CHART PANEL =====
    JPanel  ringPanel;
    int     ringPresent, ringAbsent, ringLate, ringTotal;

    // ===== STAT LABELS inside ring card =====
    JLabel lblRingPresent, lblRingAbsent, lblRingLate, lblRingTotal;
    JLabel lblRingPctPresent;

    // ===== LOG TABLE =====
    JTable            logTable;
    DefaultTableModel logModel;

    // ===== TODAY INFO =====
    JLabel lblTodayDate, lblCourseInfo;

    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    static final Color CLR_PRESENT = new Color(40,  167, 69);
    static final Color CLR_ABSENT  = new Color(220, 53,  69);
    static final Color CLR_LATE    = new Color(255, 193, 7);
    static final DateTimeFormatter DISP_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public TodayAttendanceLog() {

        setTitle("Today's Attendance Log");
        setSize(1200, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = UIUtils.mainPanel();

        // ── Title ──
        JLabel title = UIUtils.bold("Today's Attendance Log", 28);
        title.setBounds(30, 12, 600, 42);
        main.add(title);

        // ── Today date ──
        lblTodayDate = UIUtils.plain(
            "Date: " + new SimpleDateFormat("dd-MM-yyyy")
                .format(Calendar.getInstance().getTime()), 13);
        lblTodayDate.setForeground(new Color(100, 100, 110));
        lblTodayDate.setBounds(30, 46, 300, 22);
        main.add(lblTodayDate);

        // ─────────────────────────────────────────
        //  FILTER BAR
        // ─────────────────────────────────────────
        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        filterBar.setBounds(30, 72, 1120, 60);
        main.add(filterBar);

        JLabel lCrs = UIUtils.plain("Course:", 13);
        lCrs.setBounds(15, 16, 60, 28);
        filterBar.add(lCrs);

        courseBox = new JComboBox<>();
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseBox.setBounds(78, 14, 300, 32);
        filterBar.add(courseBox);

        btnRefresh = UIUtils.colorButton("Refresh",
            UIUtils.CLR_BLUE, 392, 14, 110, 32);
        filterBar.add(btnRefresh);

        btnBack = UIUtils.colorButton("← Back",
            UIUtils.CLR_GRAY, 516, 14, 100, 32);
        filterBar.add(btnBack);

        lblCourseInfo = UIUtils.plain("", 12);
        lblCourseInfo.setForeground(new Color(100, 100, 110));
        lblCourseInfo.setBounds(640, 18, 460, 24);
        filterBar.add(lblCourseInfo);

        // ─────────────────────────────────────────
        //  LEFT — RING CHART CARD
        // ─────────────────────────────────────────
        JPanel ringCard = new JPanel(null);
        ringCard.setBackground(Color.WHITE);
        ringCard.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        ringCard.setBounds(30, 145, 380, 530);
        main.add(ringCard);

        JLabel lRingTitle = UIUtils.bold("Attendance Overview", 15);
        lRingTitle.setBounds(20, 16, 340, 26);
        ringCard.add(lRingTitle);

        // ring drawing area
        ringPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawRing((Graphics2D) g);
            }
        };
        ringPanel.setBackground(Color.WHITE);
        ringPanel.setBounds(40, 50, 300, 300);
        ringCard.add(ringPanel);

        // ── Centre % label inside ring ──
        lblRingPctPresent = new JLabel("0%", JLabel.CENTER);
        lblRingPctPresent.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblRingPctPresent.setForeground(CLR_PRESENT);
        lblRingPctPresent.setBounds(90, 118, 120, 65);
        ringPanel.add(lblRingPctPresent);

        JLabel lblPresent2 = new JLabel("Present", JLabel.CENTER);
        lblPresent2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPresent2.setForeground(new Color(120, 120, 130));
        lblPresent2.setBounds(78, 178, 145, 18);
        ringPanel.add(lblPresent2);

        // ── Legend ──
        int ly = 365;
        addLegendItem(ringCard, "Present", CLR_PRESENT,  20, ly);
        addLegendItem(ringCard, "Absent",  CLR_ABSENT,  135, ly);
        addLegendItem(ringCard, "Late",    CLR_LATE,    250, ly);

        // ── Stat numbers ──
        lblRingPresent = bigStat(ringCard, "0", "Present", CLR_PRESENT,  20, 400);
        lblRingAbsent  = bigStat(ringCard, "0", "Absent",  CLR_ABSENT,  135, 400);
        lblRingLate    = bigStat(ringCard, "0", "Late",    CLR_LATE,    250, 400);

        // total
        JLabel lTot = UIUtils.plain("TOTAL STUDENTS", 10);
        lTot.setForeground(new Color(130, 130, 140));
        lTot.setBounds(20, 470, 340, 16);
        ringCard.add(lTot);

        lblRingTotal = UIUtils.bold("0", 20);
        lblRingTotal.setForeground(new Color(30, 30, 40));
        lblRingTotal.setBounds(20, 488, 340, 28);
        ringCard.add(lblRingTotal);

        // ─────────────────────────────────────────
        //  RIGHT — LOG TABLE
        // ─────────────────────────────────────────
        JLabel lLogTitle = UIUtils.bold("Student-wise Status", 15);
        lLogTitle.setBounds(430, 145, 720, 26);
        main.add(lLogTitle);

        String[] cols = {"#", "Student Name", "Status", "Remarks"};
        logModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        logTable = new JTable(logModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r,
                    int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String status = logModel.getValueAt(row, 2) != null
                        ? logModel.getValueAt(row, 2).toString() : "";
                    switch (status) {
                        case "Present":
                            c.setBackground(new Color(236, 253, 240)); break;
                        case "Absent":
                            c.setBackground(new Color(255, 235, 235)); break;
                        case "Late":
                            c.setBackground(new Color(255, 248, 220)); break;
                        default:
                            c.setBackground(row%2==0 ? Color.WHITE
                                : new Color(245, 247, 250));
                    }
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        };

        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.setRowHeight(38);
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        logTable.getTableHeader().setBackground(new Color(0, 102, 204));
        logTable.getTableHeader().setForeground(Color.WHITE);
        logTable.getTableHeader().setPreferredSize(new Dimension(0, 42));
        logTable.setGridColor(new Color(230, 230, 230));
        logTable.setShowVerticalLines(false);
        logTable.setFillsViewportHeight(true);
        logTable.setSelectionBackground(new Color(184, 207, 229));

        ((DefaultTableCellRenderer) logTable.getTableHeader()
            .getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // colour Status column
        logTable.getColumnModel().getColumn(2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel,
                        boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                    setHorizontalAlignment(JLabel.CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (!sel && v != null) {
                        switch (v.toString()) {
                            case "Present":
                                setForeground(CLR_PRESENT); break;
                            case "Absent":
                                setForeground(CLR_ABSENT);  break;
                            case "Late":
                                setForeground(new Color(160, 120, 0)); break;
                            default:
                                setForeground(Color.BLACK);
                        }
                    }
                    return this;
                }
            });

        JScrollPane logScroll = UIUtils.scrollPane(
            logTable, 430, 178, 720, 497);
        logScroll.setBorder(BorderFactory.createLineBorder(
            new Color(210, 215, 220)));
        main.add(logScroll);

        add(main);

        // ── Load courses ──
        loadCourseDropdown();

        // ── Listeners ──
        courseBox.addActionListener(e -> loadTodayLog());

        btnRefresh.addActionListener(e -> loadTodayLog());

        btnBack.addActionListener(e -> dispose());

        // ── Load on open ──
        loadTodayLog();

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  LOAD COURSE DROPDOWN
    // ─────────────────────────────────────────
    private void loadCourseDropdown() {
        courseBox.removeAllItems();
        courseBox.addItem("All Courses");
        try {
            pst = con.prepareStatement(
                "SELECT course_name FROM courses ORDER BY course_name");
            rs = pst.executeQuery();
            while (rs.next())
                courseBox.addItem(rs.getString("course_name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  LOAD TODAY'S LOG
    // ─────────────────────────────────────────
    private void loadTodayLog() {

        String today = new SimpleDateFormat("yyyy-MM-dd")
            .format(Calendar.getInstance().getTime());

        String selCourse = courseBox.getSelectedItem() != null
            ? courseBox.getSelectedItem().toString() : "All Courses";
        boolean allCourses = selCourse.equals("All Courses");

        logModel.setRowCount(0);
        ringPresent = 0;
        ringAbsent  = 0;
        ringLate    = 0;

        try {
            StringBuilder q = new StringBuilder(
                "SELECT s.name, a.status, a.remarks, c.course_name " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN courses  c ON a.course_id  = c.id " +
                "WHERE a.attendance_date = ? ");

            if (!allCourses)
                q.append("AND c.course_name = ? ");

            q.append("ORDER BY s.name");

            pst = con.prepareStatement(q.toString());
            pst.setString(1, today);
            if (!allCourses) pst.setString(2, selCourse);

            rs = pst.executeQuery();
            int rowNum = 1;

            while (rs.next()) {
                String status = rs.getString("a.status");

                if ("Present".equals(status)) ringPresent++;
                else if ("Absent".equals(status)) ringAbsent++;
                else if ("Late".equals(status))    ringLate++;

                logModel.addRow(new Object[]{
                    rowNum++,
                    rs.getString("s.name"),
                    status,
                    rs.getString("a.remarks")
                });
            }

            ringTotal = ringPresent + ringAbsent + ringLate;

            // update ring stats
            double pct = ringTotal > 0
                ? (ringPresent * 100.0 / ringTotal) : 0;

            lblRingPctPresent.setText(String.format("%.0f%%", pct));
            lblRingPresent.setText(String.valueOf(ringPresent));
            lblRingAbsent .setText(String.valueOf(ringAbsent));
            lblRingLate   .setText(String.valueOf(ringLate));
            lblRingTotal  .setText(String.valueOf(ringTotal));

            lblCourseInfo.setText(
                allCourses
                    ? "Showing all courses  |  " + ringTotal + " records today"
                    : selCourse + "  |  " + ringTotal + " students today");

            // repaint the ring
            ringPanel.repaint();

            TableUtils.resizeColumnWidth(logTable);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  DRAW RING CHART
    // ─────────────────────────────────────────
    private void drawRing(Graphics2D g2) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = 50, cy = 50, size = 200, stroke = 36;

        if (ringTotal == 0) {
            // empty grey ring
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(stroke,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2.drawOval(cx, cy, size, size);
            return;
        }

        double presentDeg = (ringPresent * 360.0 / ringTotal);
        double absentDeg  = (ringAbsent  * 360.0 / ringTotal);
        double lateDeg    = 360.0 - presentDeg - absentDeg;

        g2.setStroke(new BasicStroke(stroke,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        // gap between segments
        double gap = ringTotal > 1 ? 2.0 : 0;

        double start = -90; // start from top

        // Present (green)
        if (ringPresent > 0) {
            g2.setColor(CLR_PRESENT);
            g2.draw(new Arc2D.Double(cx, cy, size, size,
                start, presentDeg - gap, Arc2D.OPEN));
        }
        start += presentDeg;

        // Absent (red)
        if (ringAbsent > 0) {
            g2.setColor(CLR_ABSENT);
            g2.draw(new Arc2D.Double(cx, cy, size, size,
                start, absentDeg - gap, Arc2D.OPEN));
        }
        start += absentDeg;

        // Late (yellow)
        if (ringLate > 0) {
            g2.setColor(CLR_LATE);
            g2.draw(new Arc2D.Double(cx, cy, size, size,
                start, lateDeg - gap, Arc2D.OPEN));
        }

        // white inner circle to make it a ring
        g2.setColor(Color.WHITE);
        int inner = stroke + 8;
        g2.fillOval(cx + inner/2, cy + inner/2,
                    size - inner, size - inner);
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private void addLegendItem(JPanel parent, String label,
                                Color color, int x, int y) {
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setBounds(x, y + 4, 12, 12);
        parent.add(dot);

        JLabel lbl = UIUtils.plain(label, 12);
        lbl.setForeground(new Color(60, 60, 70));
        lbl.setBounds(x + 16, y, 80, 20);
        parent.add(lbl);
    }

    private JLabel bigStat(JPanel parent, String value,
                            String subLabel, Color color,
                            int x, int y) {
        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lVal.setForeground(color);
        lVal.setBounds(x, y, 100, 34);
        parent.add(lVal);

        JLabel lSub = UIUtils.plain(subLabel, 11);
        lSub.setForeground(new Color(130, 130, 140));
        lSub.setBounds(x, y + 34, 100, 16);
        parent.add(lSub);

        return lVal;
    }
}