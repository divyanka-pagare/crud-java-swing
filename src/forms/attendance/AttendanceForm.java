package src.forms.attendance;

import src.db.DBConnection;
import src.models.Course;
import src.models.Student;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
// import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceForm extends JFrame {

    // ===== FILTER CONTROLS =====
    JComboBox<Course>   courseBox;
    JSpinner            datePicker;
    JButton             btnLoad, btnSaveAll, btnMarkAllPresent, btnClear;

    // ===== STATUS LABELS =====
    JLabel lblStatus, lblDate, lblCourse, lblTotalStudents;
    JLabel lblPresentCount, lblAbsentCount, lblLateCount;

    // ===== ATTENDANCE TABLE =====
    JTable            attendanceTable;
    DefaultTableModel tableModel;

    // ===== SUMMARY STRIP =====
    JPanel summaryPanel;

    // ===== RECENT LOG TABLE =====
    JTable            logTable;
    DefaultTableModel logModel;
    JComboBox<String> logCourseFilter;

    // ===== DATA =====
    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    List<Student> enrolledStudents = new ArrayList<>();

    static final DateTimeFormatter DB_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter DISP_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    static final Color CLR_PRESENT = new Color(40,  167, 69);
    static final Color CLR_ABSENT  = new Color(220, 53,  69);
    static final Color CLR_LATE    = new Color(255, 193, 7);

    public AttendanceForm() {

        setTitle("Mark Attendance");
        setSize(1200, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = UIUtils.mainPanel();

        // ── Page Title ──
        JLabel title = UIUtils.bold("Mark Attendance", 28);
        title.setBounds(30, 12, 400, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  FILTER BAR
        // ─────────────────────────────────────────
        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        filterBar.setBounds(30, 62, 1120, 70);
        main.add(filterBar);

        // Course label + dropdown
        JLabel lCourse = UIUtils.plain("Course:", 13);
        lCourse.setBounds(15, 22, 60, 26);
        filterBar.add(lCourse);
        logCourseFilter = new JComboBox<>();
        logCourseFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logCourseFilter.setBounds(920, 238, 230, 28);
        main.add(logCourseFilter);

        courseBox = new JComboBox<>();
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseBox.setBounds(78, 20, 260, 32);
        filterBar.add(courseBox);

        // Date label + spinner
        JLabel lDate = UIUtils.plain("Date:", 13);
        lDate.setBounds(360, 22, 45, 26);
        filterBar.add(lDate);

        datePicker = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(datePicker, "dd-MM-yyyy");
        datePicker.setEditor(dateEditor);
        datePicker.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        datePicker.setBounds(408, 20, 145, 32);
        filterBar.add(datePicker);

        // set default date to today
        datePicker.setValue(Calendar.getInstance().getTime());

        // Buttons
        btnLoad = UIUtils.colorButton("Load Students",
            UIUtils.CLR_BLUE, 570, 18, 140, 34);
        btnMarkAllPresent = UIUtils.colorButton("Mark All Present",
            CLR_PRESENT, 724, 18, 155, 34);
        btnSaveAll = UIUtils.colorButton("Save Attendance",
            new Color(0, 102, 153), 894, 18, 155, 34);
        btnClear = UIUtils.colorButton("Clear",
            UIUtils.CLR_GRAY, 1064, 18, 80, 34);

        filterBar.add(btnLoad);
        filterBar.add(btnMarkAllPresent);
        filterBar.add(btnSaveAll);
        filterBar.add(btnClear);

        // ─────────────────────────────────────────
        //  SUMMARY STRIP
        // ─────────────────────────────────────────
        summaryPanel = new JPanel(null);
        summaryPanel.setBackground(new Color(245, 247, 250));
        summaryPanel.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        summaryPanel.setBounds(30, 145, 1120, 80);
        main.add(summaryPanel);

        lblTotalStudents = summaryItem(summaryPanel, "Total Students", "0",
            new Color(0, 102, 204), 0);
        lblPresentCount  = summaryItem(summaryPanel, "Present",        "0",
            CLR_PRESENT,            1);
        lblAbsentCount   = summaryItem(summaryPanel, "Absent",         "0",
            CLR_ABSENT,             2);
        lblLateCount     = summaryItem(summaryPanel, "Late",           "0",
            CLR_LATE,               3);

        // status label (right side of summary)
        lblStatus = new JLabel("Select a course and date, then click Load Students");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(new Color(100, 100, 110));
        lblStatus.setBounds(1135, 28, 400, 24);
        summaryPanel.add(lblStatus);

        // ─────────────────────────────────────────
        //  LEFT — ATTENDANCE TABLE
        // ─────────────────────────────────────────
        JLabel lAttTitle = UIUtils.bold("Students", 14);
        lAttTitle.setBounds(30, 238, 200, 28);
        main.add(lAttTitle);

        // table columns: #, Student Name, Status (radio-style), Remarks
        String[] cols = {"#", "Student Name", "Present", "Absent", "Late", "Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 2 || col == 3 || col == 4) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col >= 2;
            }
        };

        attendanceTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    // colour row based on current status
                    Object p = getModel().getValueAt(row, 2);
                    Object a = getModel().getValueAt(row, 3);
                    Object l = getModel().getValueAt(row, 4);
                    if (Boolean.TRUE.equals(p))
                        c.setBackground(new Color(236, 253, 240));
                    else if (Boolean.TRUE.equals(a))
                        c.setBackground(new Color(255, 235, 235));
                    else if (Boolean.TRUE.equals(l))
                        c.setBackground(new Color(255, 248, 220));
                    else
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        };

        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attendanceTable.setRowHeight(38);
        attendanceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        attendanceTable.getTableHeader().setBackground(new Color(0, 102, 204));
        attendanceTable.getTableHeader().setForeground(Color.WHITE);
        attendanceTable.setGridColor(new Color(230, 230, 230));
        attendanceTable.setShowVerticalLines(false);
        attendanceTable.setIntercellSpacing(new Dimension(0, 1));
        attendanceTable.setFillsViewportHeight(true);
        attendanceTable.setSelectionBackground(new Color(184, 207, 229));

        // centre-align header
        ((DefaultTableCellRenderer) attendanceTable.getTableHeader()
            .getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(180);

        // centre checkboxes
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(JLabel.CENTER);
        attendanceTable.getColumnModel().getColumn(0).setCellRenderer(centerR);

        JScrollPane attScroll = new JScrollPane(attendanceTable);
        attScroll.setBounds(30, 270, 680, 400);
        attScroll.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        attScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        main.add(attScroll);

        // mutual exclusion: only one of P/A/L can be true per row
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col < 2 || col > 4 || row < 0) return;
            if (Boolean.TRUE.equals(tableModel.getValueAt(row, col))) {
                for (int c2 = 2; c2 <= 4; c2++) {
                    if (c2 != col) tableModel.setValueAt(false, row, c2);
                }
            }
            updateSummaryCounts();
        });

        // ─────────────────────────────────────────
        //  RIGHT — RECENT ATTENDANCE LOG
        // ─────────────────────────────────────────
        // JLabel lLogTitle = UIUtils.bold("Recent Attendance Log", 14);
        // lLogTitle.setBounds(730, 238, 280, 28);
        // main.add(lLogTitle);

        // JLabel lLogFilter = UIUtils.plain("Filter:", 12);
        // lLogFilter.setBounds(730, 242, 45, 20);
        // main.add(lLogFilter);

        logCourseFilter = new JComboBox<>();
        logCourseFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logCourseFilter.setBounds(920, 238, 230, 28);
        main.add(logCourseFilter);

        String[] logCols = {"Date", "Student", "Course", "Status"};
        logModel = new DefaultTableModel(logCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        logTable = new JTable(logModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String status = logModel.getValueAt(row, 3) != null
                        ? logModel.getValueAt(row, 3).toString() : "";
                    switch (status) {
                        case "Present": c.setBackground(new Color(236, 253, 240)); break;
                        case "Absent":  c.setBackground(new Color(255, 235, 235)); break;
                        case "Late":    c.setBackground(new Color(255, 248, 220)); break;
                        default:        c.setBackground(row%2==0 ? Color.WHITE
                                            : new Color(245,247,250));
                    }
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        };

        // logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // logTable.setRowHeight(34);
        // logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        // logTable.getTableHeader().setBackground(new Color(0, 102, 204));
        // logTable.getTableHeader().setForeground(Color.WHITE);
        // logTable.setGridColor(new Color(230, 230, 230));
        // logTable.setShowVerticalLines(false);
        // logTable.setFillsViewportHeight(true);
        // logTable.setSelectionBackground(new Color(184, 207, 229));
        // ((DefaultTableCellRenderer) logTable.getTableHeader()
        //     .getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // // colour Status column text
        // logTable.getColumnModel().getColumn(3).setCellRenderer(
        //     new DefaultTableCellRenderer() {
        //         @Override
        //         public Component getTableCellRendererComponent(
        //                 JTable t, Object v, boolean sel, boolean foc, int row, int col) {
        //             super.getTableCellRendererComponent(t,v,sel,foc,row,col);
        //             setHorizontalAlignment(JLabel.CENTER);
        //             if (!sel) {
        //                 String s = v!=null ? v.toString() : "";
        //                 switch (s) {
        //                     case "Present": setForeground(CLR_PRESENT); break;
        //                     case "Absent":  setForeground(CLR_ABSENT);  break;
        //                     case "Late":    setForeground(new Color(180,130,0)); break;
        //                     default:        setForeground(Color.BLACK);
        //                 }
        //             }
        //             return this;
        //         }
        //     });

        // JScrollPane logScroll = new JScrollPane(logTable);
        // logScroll.setBounds(730, 270, 420, 400);
        // logScroll.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        // logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // main.add(logScroll);

        // // ── Already-marked badge label ──
        // JLabel lblAlreadyMarked = new JLabel();
        // lblAlreadyMarked.setFont(new Font("Segoe UI", Font.BOLD, 11));
        // lblAlreadyMarked.setForeground(CLR_PRESENT);
        // lblAlreadyMarked.setBounds(730, 678, 420, 22);
        // main.add(lblAlreadyMarked);

        // ── View Today's Log Button ──
        JButton btnTodayLog = UIUtils.colorButton(
            "📋  Today's Attendance Log",
            new Color(0, 102, 153),
            730, 640, 420, 42);
        btnTodayLog.setFont(new Font("Segoe UI", Font.BOLD, 14));
        main.add(btnTodayLog);

        // ── Already-marked badge label ──
        JLabel lblAlreadyMarked = new JLabel();
        lblAlreadyMarked.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAlreadyMarked.setForeground(CLR_PRESENT);
        lblAlreadyMarked.setBounds(30, 678, 680, 22);
        main.add(lblAlreadyMarked);

        add(main);

        // ─────────────────────────────────────────
        //  LOAD DATA
        // ─────────────────────────────────────────
        loadCourseDropdown();
        loadLogCourseFilter();
        loadRecentLog(null);

        // ─────────────────────────────────────────
        //  LISTENERS
        // ─────────────────────────────────────────
        btnLoad.addActionListener(e -> {
            loadStudentsForAttendance();
            checkAlreadyMarked(lblAlreadyMarked);
        });

        btnTodayLog.addActionListener(e ->
            UIUtils.openFullScreen(new TodayAttendanceLog()));

        btnMarkAllPresent.addActionListener(e -> markAllPresent());

        btnSaveAll.addActionListener(e -> saveAttendance());

        btnClear.addActionListener(e -> clearForm());

        logCourseFilter.addActionListener(e -> {
            Object sel = logCourseFilter.getSelectedItem();
            if (sel == null) return;
            String filter = sel.toString();
            loadRecentLog(filter.equals("All Courses") ? null : filter);
        });

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  LOAD COURSES INTO DROPDOWN
    // ─────────────────────────────────────────
    private void loadCourseDropdown() {
        courseBox.removeAllItems();
        try {
            pst = con.prepareStatement(
                "SELECT id, course_name, fees, duration " +
                "FROM courses ORDER BY course_name");
            rs = pst.executeQuery();
            while (rs.next()) {
                courseBox.addItem(new Course(
                    rs.getInt("id"),
                    rs.getString("course_name"),
                    rs.getDouble("fees"),
                    rs.getString("duration")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadLogCourseFilter() {
        logCourseFilter.removeAllItems();
        logCourseFilter.addItem("All Courses");
        try {
            pst = con.prepareStatement(
                "SELECT course_name FROM courses ORDER BY course_name");
            rs = pst.executeQuery();
            while (rs.next())
                logCourseFilter.addItem(rs.getString("course_name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  LOAD STUDENTS FOR SELECTED COURSE + DATE
    // ─────────────────────────────────────────
    private void loadStudentsForAttendance() {

        Course course = (Course) courseBox.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course");
            return;
        }

        String dateStr = getSelectedDate();
        tableModel.setRowCount(0);
        enrolledStudents.clear();

        try {
            // get all students enrolled in this course
            pst = con.prepareStatement(
                "SELECT s.id, s.name FROM enrollments e " +
                "JOIN students s ON e.student_id = s.id " +
                "WHERE e.course_id = ? ORDER BY s.name");
            pst.setInt(1, course.getId());
            rs = pst.executeQuery();

            List<Integer> studentIds   = new ArrayList<>();
            List<String>  studentNames = new ArrayList<>();

            while (rs.next()) {
                studentIds  .add(rs.getInt("id"));
                studentNames.add(rs.getString("name"));
            }

            if (studentIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No students enrolled in: " + course.getCourseName());
                return;
            }

            // check if attendance already marked for this date+course
            int rowNum = 1;
            for (int i = 0; i < studentIds.size(); i++) {
                int    sid  = studentIds.get(i);
                String name = studentNames.get(i);

                pst = con.prepareStatement(
                    "SELECT status FROM attendance " +
                    "WHERE student_id=? AND course_id=? AND attendance_date=?");
                pst.setInt(1, sid);
                pst.setInt(2, course.getId());
                pst.setString(3, dateStr);
                rs = pst.executeQuery();

                boolean present = false, absent = false, late = false;
                String  remarks = "";

                if (rs.next()) {
                    String status = rs.getString("status");
                    present = "Present".equals(status);
                    absent  = "Absent" .equals(status);
                    late    = "Late"   .equals(status);
                } else {
                    // default to Present
                    present = true;
                }

                tableModel.addRow(new Object[]{
                    rowNum++, name, present, absent, late, remarks
                });

                Student s = new Student();
                s.setId(sid);
                s.setName(name);
                enrolledStudents.add(s);
            }

            updateSummaryCounts();
            lblStatus.setText("Loaded " + studentIds.size() +
                " students for " + course.getCourseName() +
                " on " + dateStr);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  CHECK ALREADY MARKED BADGE
    // ─────────────────────────────────────────
    private void checkAlreadyMarked(JLabel badge) {
        Course course = (Course) courseBox.getSelectedItem();
        if (course == null) return;
        String dateStr = getSelectedDate();
        try {
            pst = con.prepareStatement(
                "SELECT COUNT(*) cnt FROM attendance " +
                "WHERE course_id=? AND attendance_date=?");
            pst.setInt(1, course.getId());
            pst.setString(2, dateStr);
            rs = pst.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) {
                badge.setText("✔ Attendance already marked for this date — editing existing records");
            } else {
                badge.setText("");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  MARK ALL PRESENT
    // ─────────────────────────────────────────
    private void markAllPresent() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Load students first");
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(true,  i, 2);
            tableModel.setValueAt(false, i, 3);
            tableModel.setValueAt(false, i, 4);
        }
        updateSummaryCounts();
    }

    // ─────────────────────────────────────────
    //  SAVE ATTENDANCE
    // ─────────────────────────────────────────
    private void saveAttendance() {

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No attendance to save. Load students first.");
            return;
        }

        Course course = (Course) courseBox.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course");
            return;
        }

        String dateStr = getSelectedDate();
        int saved = 0, skipped = 0;

        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {

                if (i >= enrolledStudents.size()) continue;

                int     sid     = enrolledStudents.get(i).getId();
                boolean present = Boolean.TRUE.equals(tableModel.getValueAt(i, 2));
                boolean absent  = Boolean.TRUE.equals(tableModel.getValueAt(i, 3));
                boolean late    = Boolean.TRUE.equals(tableModel.getValueAt(i, 4));
                Object  remObj  = tableModel.getValueAt(i, 5);
                String  remarks = remObj != null ? remObj.toString() : "";

                // must select one status
                if (!present && !absent && !late) {
                    skipped++;
                    continue;
                }

                String status = present ? "Present" : absent ? "Absent" : "Late";

                // upsert: insert or update if already exists
                pst = con.prepareStatement(
                    "INSERT INTO attendance " +
                    "(student_id, course_id, attendance_date, status, remarks) " +
                    "VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE status=VALUES(status), remarks=VALUES(remarks)");
                pst.setInt(1, sid);
                pst.setInt(2, course.getId());
                pst.setString(3, dateStr);
                pst.setString(4, status);
                pst.setString(5, remarks);
                pst.executeUpdate();
                saved++;
            }

            String msg = saved + " attendance record(s) saved.";
            if (skipped > 0) msg += "\n" + skipped + " row(s) skipped (no status selected).";
            JOptionPane.showMessageDialog(this, msg, "Saved", JOptionPane.INFORMATION_MESSAGE);

            loadRecentLog(null);
            updateSummaryCounts();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────
    //  LOAD RECENT LOG (right side table)
    // ─────────────────────────────────────────
    private void loadRecentLog(String courseFilter) {
        logModel.setRowCount(0);
        try {
            StringBuilder q = new StringBuilder(
                "SELECT a.attendance_date, s.name, c.course_name, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "JOIN courses  c ON a.course_id  = c.id ");
            if (courseFilter != null && !courseFilter.isBlank())
                q.append("WHERE c.course_name = ? ");
            q.append("ORDER BY a.attendance_date DESC, s.name LIMIT 200");

            pst = con.prepareStatement(q.toString());
            if (courseFilter != null && !courseFilter.isBlank())
                pst.setString(1, courseFilter);

            rs = pst.executeQuery();
            while (rs.next()) {
                Date   d    = rs.getDate("attendance_date");
                String disp = d != null
                    ? d.toLocalDate().format(DISP_FMT) : "—";
                logModel.addRow(new Object[]{
                    disp,
                    rs.getString("s.name"),
                    rs.getString("c.course_name"),
                    rs.getString("a.status")
                });
            }
            TableUtils.resizeColumnWidth(logTable);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  UPDATE SUMMARY COUNTS
    // ─────────────────────────────────────────
    private void updateSummaryCounts() {
        int total = tableModel.getRowCount();
        int p = 0, a = 0, l = 0;
        for (int i = 0; i < total; i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 2))) p++;
            else if (Boolean.TRUE.equals(tableModel.getValueAt(i, 3))) a++;
            else if (Boolean.TRUE.equals(tableModel.getValueAt(i, 4))) l++;
        }
        lblTotalStudents.setText(String.valueOf(total));
        lblPresentCount .setText(String.valueOf(p));
        lblAbsentCount  .setText(String.valueOf(a));
        lblLateCount    .setText(String.valueOf(l));
    }

    // ─────────────────────────────────────────
    //  CLEAR FORM
    // ─────────────────────────────────────────
    private void clearForm() {
        tableModel.setRowCount(0);
        enrolledStudents.clear();
        lblTotalStudents.setText("0");
        lblPresentCount .setText("0");
        lblAbsentCount  .setText("0");
        lblLateCount    .setText("0");
        lblStatus.setText("Select a course and date, then click Load Students");
        datePicker.setValue(Calendar.getInstance().getTime());
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private String getSelectedDate() {
        java.util.Date d = (java.util.Date) datePicker.getValue();
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private JLabel summaryItem(JPanel parent, String heading,
                                String value, Color accent, int index) {
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
        card.setBounds(x, 10, cardW, 60);
        parent.add(card);

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setBounds(14, 12, 7, 7);
        card.add(dot);

        JLabel lHead = new JLabel(heading.toUpperCase());
        lHead.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lHead.setForeground(new Color(130, 130, 140));
        lHead.setBounds(28, 10, cardW - 32, 14);
        card.add(lHead);

        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lVal.setForeground(new Color(30, 30, 40));
        lVal.setBounds(14, 26, cardW - 18, 28);
        card.add(lVal);

        return lVal;
    }
}