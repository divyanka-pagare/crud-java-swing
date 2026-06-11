package src.forms.attendance;

import src.db.DBConnection;
import src.models.Course;
import src.utils.QRCodeGenerator;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class QRAttendanceForm extends JFrame {

    // ===== FILTER =====
    JComboBox<Course> courseBox;
    JSpinner          datePicker;
    JButton           btnGenerate, btnRefresh, btnExpire, btnSaveQR;

    // ===== QR PANEL =====
    JPanel  qrCard;
    JLabel  lblQRImage;
    JLabel  lblQRStatus;
    JLabel  lblTimer;
    JLabel  lblSessionToken;
    JLabel  lblScanCount;

    // ===== SCAN INPUT (simulates camera scan) =====
    JTextField txtStudentSearch;
    JButton    btnScan;
    JLabel     lblScanFeedback;

    // ===== ATTENDANCE TABLE =====
    JTable            attTable;
    DefaultTableModel attModel;

    // ===== TIMER =====
    Timer      qrTimer;
    int        secondsLeft = 0;
    static final int QR_VALID_SECONDS = 300; // 5 minutes

    // ===== STATE =====
    String  currentToken    = null;
    boolean sessionActive   = false;
    int     currentCourseId = -1;
    String  currentDate     = null;

    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    static final Color CLR_PRESENT = new Color(40,  167, 69);
    static final Color CLR_EXPIRED = new Color(220, 53,  69);
    static final Color CLR_ACTIVE  = new Color(0,   102, 204);
    static final DateTimeFormatter DISP_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public QRAttendanceForm() {

        setTitle("QR Code Attendance");
        setSize(1200, 750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = UIUtils.mainPanel();

        // ── Title ──
        JLabel title = UIUtils.bold("QR Code Attendance", 28);
        title.setBounds(30, 12, 500, 42);
        main.add(title);

        JLabel sub = UIUtils.plain(
            "Generate a QR code for the class — students scan to mark attendance",
            13);
        sub.setForeground(new Color(100, 100, 110));
        sub.setBounds(30, 46, 700, 22);
        main.add(sub);

        // ─────────────────────────────────────────
        //  FILTER BAR
        // ─────────────────────────────────────────
        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(
            new Color(210, 215, 220)));
        filterBar.setBounds(30, 72, 1120, 62);
        main.add(filterBar);

        JLabel lCrs = UIUtils.plain("Course:", 13);
        lCrs.setBounds(15, 18, 60, 26);
        filterBar.add(lCrs);

        courseBox = new JComboBox<>();
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseBox.setBounds(78, 16, 280, 32);
        filterBar.add(courseBox);

        JLabel lDate = UIUtils.plain("Date:", 13);
        lDate.setBounds(378, 18, 45, 26);
        filterBar.add(lDate);

        datePicker = new JSpinner(new SpinnerDateModel());
        datePicker.setEditor(
            new JSpinner.DateEditor(datePicker, "dd-MM-yyyy"));
        datePicker.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        datePicker.setBounds(426, 16, 145, 32);
        datePicker.setValue(Calendar.getInstance().getTime());
        filterBar.add(datePicker);

        btnGenerate = UIUtils.colorButton(
            "Generate QR", CLR_ACTIVE, 590, 14, 140, 34);
        btnRefresh  = UIUtils.colorButton(
            "Refresh", UIUtils.CLR_GRAY, 744, 14, 100, 34);
        btnExpire   = UIUtils.colorButton(
            "Expire QR", CLR_EXPIRED, 858, 14, 110, 34);
        btnSaveQR   = UIUtils.colorButton(
            "Save QR Image", new Color(40, 167, 69), 982, 14, 140, 34);

        filterBar.add(btnGenerate);
        filterBar.add(btnRefresh);
        filterBar.add(btnExpire);
        filterBar.add(btnSaveQR);

        btnExpire .setEnabled(false);
        btnSaveQR .setEnabled(false);

        // ─────────────────────────────────────────
        //  LEFT — QR CODE CARD
        // ─────────────────────────────────────────
        qrCard = new JPanel(null);
        qrCard.setBackground(Color.WHITE);
        qrCard.setBorder(BorderFactory.createLineBorder(
            new Color(210, 215, 220)));
        qrCard.setBounds(30, 148, 400, 530);
        main.add(qrCard);

        JLabel lQRTitle = UIUtils.bold("Session QR Code", 16);
        lQRTitle.setBounds(20, 18, 360, 28);
        qrCard.add(lQRTitle);

        // QR image label
        lblQRImage = new JLabel();
        lblQRImage.setHorizontalAlignment(JLabel.CENTER);
        lblQRImage.setVerticalAlignment(JLabel.CENTER);
        lblQRImage.setBounds(50, 55, 300, 300);
        lblQRImage.setBackground(new Color(245, 247, 250));
        lblQRImage.setOpaque(true);
        lblQRImage.setBorder(BorderFactory.createDashedBorder(
            new Color(180, 180, 190), 4, 6, 3, false));

        // placeholder text
        lblQRImage.setText(
            "<html><center><br><br>Select course and date<br>" +
            "then click<br><b>Generate QR</b></center></html>");
        lblQRImage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQRImage.setForeground(new Color(160, 160, 170));
        qrCard.add(lblQRImage);

        // status badge
        lblQRStatus = new JLabel("Not Started");
        lblQRStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQRStatus.setForeground(new Color(130, 130, 140));
        lblQRStatus.setHorizontalAlignment(JLabel.CENTER);
        lblQRStatus.setBounds(50, 362, 300, 26);
        qrCard.add(lblQRStatus);

        // timer
        lblTimer = new JLabel("—");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTimer.setForeground(CLR_ACTIVE);
        lblTimer.setHorizontalAlignment(JLabel.CENTER);
        lblTimer.setBounds(50, 390, 300, 34);
        qrCard.add(lblTimer);

        JLabel lTimerSub = UIUtils.plain("time remaining", 11);
        lTimerSub.setForeground(new Color(160, 160, 170));
        lTimerSub.setHorizontalAlignment(JLabel.CENTER);
        lTimerSub.setBounds(50, 422, 300, 18);
        qrCard.add(lTimerSub);

        // session token (small)
        lblSessionToken = new JLabel("Session token: —");
        lblSessionToken.setFont(new Font("Courier New", Font.PLAIN, 10));
        lblSessionToken.setForeground(new Color(160, 160, 170));
        lblSessionToken.setHorizontalAlignment(JLabel.CENTER);
        lblSessionToken.setBounds(20, 448, 360, 18);
        qrCard.add(lblSessionToken);

        // scan count
        lblScanCount = new JLabel("0 students scanned");
        lblScanCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblScanCount.setForeground(CLR_PRESENT);
        lblScanCount.setHorizontalAlignment(JLabel.CENTER);
        lblScanCount.setBounds(50, 470, 300, 24);
        qrCard.add(lblScanCount);

        // ─────────────────────────────────────────
        //  MIDDLE — SCAN INPUT
        //  (simulates what a camera scan would send)
        // ─────────────────────────────────────────
        JPanel scanCard = new JPanel(null);
        scanCard.setBackground(Color.WHITE);
        scanCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            "Student Check-in  (simulates QR scan)",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        scanCard.setBounds(30, 688, 1120, 82);
        main.add(scanCard);

        JLabel lSearch = UIUtils.plain("Student Name / ID:", 13);
        lSearch.setBounds(16, 26, 160, 26);
        scanCard.add(lSearch);

        txtStudentSearch = new JTextField();
        txtStudentSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtStudentSearch.setBounds(178, 24, 360, 34);
        txtStudentSearch.setToolTipText("Type student name or ID");
        scanCard.add(txtStudentSearch);

        btnScan = UIUtils.colorButton(
            "Mark Present (Scan)", CLR_PRESENT, 552, 22, 200, 38);
        btnScan.setEnabled(false);
        scanCard.add(btnScan);

        lblScanFeedback = new JLabel();
        lblScanFeedback.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblScanFeedback.setBounds(770, 28, 340, 28);
        scanCard.add(lblScanFeedback);

        // ─────────────────────────────────────────
        //  RIGHT — ATTENDANCE TABLE
        // ─────────────────────────────────────────
        JLabel lAttTitle = UIUtils.bold("Attendance for This Session", 15);
        lAttTitle.setBounds(450, 148, 700, 28);
        main.add(lAttTitle);

        String[] cols = {
            "#", "Student Name", "Status", "Marked At"
        };
        attModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        attTable = new JTable(attModel) {
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer r,
                    int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    String st = attModel.getValueAt(row, 2) != null
                        ? attModel.getValueAt(row, 2).toString() : "";
                    c.setBackground("Present".equals(st)
                        ? new Color(236, 253, 240)
                        : row%2==0 ? Color.WHITE
                                   : new Color(245, 247, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        };

        attTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attTable.setRowHeight(36);
        attTable.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 13));
        attTable.getTableHeader().setBackground(new Color(0, 102, 204));
        attTable.getTableHeader().setForeground(Color.WHITE);
        attTable.getTableHeader().setPreferredSize(
            new Dimension(0, 42));
        attTable.setGridColor(new Color(230, 230, 230));
        attTable.setShowVerticalLines(false);
        attTable.setFillsViewportHeight(true);
        attTable.setSelectionBackground(new Color(184, 207, 229));

        ((DefaultTableCellRenderer) attTable.getTableHeader()
            .getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);

        // colour Status column
        attTable.getColumnModel().getColumn(2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel,
                        boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                    setHorizontalAlignment(JLabel.CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (!sel && v != null)
                        setForeground("Present".equals(v.toString())
                            ? CLR_PRESENT : Color.BLACK);
                    return this;
                }
            });

        JScrollPane attScroll = UIUtils.scrollPane(
            attTable, 450, 182, 700, 496);
        attScroll.setBorder(BorderFactory.createLineBorder(
            new Color(210, 215, 220)));
        main.add(attScroll);

        add(main);

        // ── Load data ──
        loadCourses();

        // ── Listeners ──
        btnGenerate.addActionListener(e -> generateQR());
        btnRefresh .addActionListener(e -> refreshTable());
        btnExpire  .addActionListener(e -> expireSession());
        btnSaveQR  .addActionListener(e -> saveQRImage());

        btnScan.addActionListener(e -> markAttendanceBySearch());

        // Enter key in search box
        txtStudentSearch.addActionListener(e -> markAttendanceBySearch());

        // expire session when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopTimer();
            }
        });

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  LOAD COURSES
    // ─────────────────────────────────────────
    private void loadCourses() {
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

    // ─────────────────────────────────────────
    //  GENERATE QR
    // ─────────────────────────────────────────
    private void generateQR() {

        Course course = (Course) courseBox.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a course first");
            return;
        }

        stopTimer(); // stop any existing session

        currentCourseId = course.getId();
        currentDate     = getSelectedDate();
        currentToken    = QRCodeGenerator.generateToken();

        try {

            Connection con =
                DBConnection.getConnection();
        
            PreparedStatement pst =
                con.prepareStatement(
                    "INSERT INTO qr_sessions " +
                    "(token, course_id, attendance_date, expiry_time, active) " +
                    "VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE), TRUE)"
                );
        
            pst.setString(1, currentToken);
            pst.setInt(2, currentCourseId);
        
            pst.setDate(
                3,
                java.sql.Date.valueOf(LocalDate.now())
            );
        
            pst.executeUpdate();
        
            pst.close();
        
        } catch (SQLException ex) {
        
            ex.printStackTrace();
        
        }

        sessionActive   = true;
        secondsLeft     = QR_VALID_SECONDS;

        String content = "http://192.168.1.9:8080/attendance?token=" + currentToken;

        try {
            BufferedImage qrImage = QRCodeGenerator.generate(content, 280, 280);
            lblQRImage.setIcon(new ImageIcon(qrImage));
            lblQRImage.setText(null);
            lblQRImage.setBorder(BorderFactory.createLineBorder(
                new Color(0, 102, 204), 2));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "QR generation failed: " + ex.getMessage());
            return;
        }

        lblQRStatus.setText("● ACTIVE");
        lblQRStatus.setForeground(CLR_PRESENT);
        lblSessionToken.setText("Token: " + currentToken);
        attModel.setRowCount(0);

        btnScan   .setEnabled(true);
        btnExpire .setEnabled(true);
        btnSaveQR .setEnabled(true);
        btnGenerate.setText("Regenerate QR");

        startTimer();
        refreshTable();

        setFeedback("QR generated for " +
            course.getCourseName() + " on " + currentDate, CLR_PRESENT);
    }

    // ─────────────────────────────────────────
    //  TIMER
    // ─────────────────────────────────────────
    private void startTimer() {
        qrTimer = new Timer();
        qrTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    secondsLeft--;
                    int m = secondsLeft / 60;
                    int s = secondsLeft % 60;
                    lblTimer.setText(
                        String.format("%02d:%02d", m, s));

                    if (secondsLeft <= 60)
                        lblTimer.setForeground(CLR_EXPIRED);
                    else if (secondsLeft <= 120)
                        lblTimer.setForeground(new Color(200, 140, 0));
                    else
                        lblTimer.setForeground(CLR_ACTIVE);

                    if (secondsLeft <= 0) {
                        stopTimer();
                        onQRExpired();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (qrTimer != null) {
            qrTimer.cancel();
            qrTimer = null;
        }
    }

    private void onQRExpired() {
        sessionActive = false;
        lblQRStatus.setText("● EXPIRED");
        lblQRStatus.setForeground(CLR_EXPIRED);
        lblTimer.setText("00:00");
        lblTimer.setForeground(CLR_EXPIRED);
        btnScan.setEnabled(false);

        // dim the QR image
        if (lblQRImage.getIcon() != null) {
            lblQRImage.setBorder(BorderFactory.createLineBorder(
                CLR_EXPIRED, 2));
        }

        setFeedback("QR session expired. Generate a new one.", CLR_EXPIRED);
    }

    // ─────────────────────────────────────────
    //  EXPIRE SESSION MANUALLY
    // ─────────────────────────────────────────
    private void expireSession() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Expire the current QR session?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        stopTimer();
        onQRExpired();
    }

    // ─────────────────────────────────────────
    //  MARK ATTENDANCE BY STUDENT SEARCH
    //  (this is what a real QR scanner would trigger)
    // ─────────────────────────────────────────
    private void markAttendanceBySearch() {

        if (!sessionActive) {
            setFeedback("No active QR session. Generate a QR first.",
                CLR_EXPIRED);
            return;
        }

        String query = txtStudentSearch.getText().trim();
        if (query.isEmpty()) {
            setFeedback("Enter student name or ID to scan.", CLR_EXPIRED);
            return;
        }

        try {
            // find student by name or id in enrolled list for this course
            pst = con.prepareStatement(
                "SELECT s.id, s.name FROM students s " +
                "JOIN enrollments e ON e.student_id = s.id " +
                "WHERE e.course_id = ? " +
                "AND (s.name LIKE ? OR s.id = ?) " +
                "LIMIT 1");
            pst.setInt(1, currentCourseId);
            pst.setString(2, "%" + query + "%");
            try {
                pst.setInt(3, Integer.parseInt(query));
            } catch (NumberFormatException nfe) {
                pst.setInt(3, -1);
            }

            rs = pst.executeQuery();

            if (!rs.next()) {
                setFeedback("Student '" + query +
                    "' not found or not enrolled in this course.",
                    CLR_EXPIRED);
                txtStudentSearch.selectAll();
                return;
            }

            int    sid  = rs.getInt("id");
            String name = rs.getString("name");

            // check if already marked
            pst = con.prepareStatement(
                "SELECT id FROM attendance " +
                "WHERE student_id=? AND course_id=? " +
                "AND attendance_date=?");
            pst.setInt(1, sid);
            pst.setInt(2, currentCourseId);
            pst.setString(3, currentDate);
            rs = pst.executeQuery();

            if (rs.next()) {
                setFeedback("✓ " + name +
                    " already marked Present for today.",
                    new Color(200, 140, 0));
                txtStudentSearch.selectAll();
                return;
            }

            // mark present
            pst = con.prepareStatement(
                "INSERT INTO attendance " +
                "(student_id, course_id, attendance_date, status, remarks) " +
                "VALUES (?,?,?,'Present','QR Scan') " +
                "ON DUPLICATE KEY UPDATE " +
                "status='Present', remarks='QR Scan'");
            pst.setInt(1, sid);
            pst.setInt(2, currentCourseId);
            pst.setString(3, currentDate);
            pst.executeUpdate();

            setFeedback("✓ " + name + " marked Present!", CLR_PRESENT);
            txtStudentSearch.setText("");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            setFeedback("Error: " + ex.getMessage(), CLR_EXPIRED);
        }
    }

    // ─────────────────────────────────────────
    //  REFRESH TABLE
    // ─────────────────────────────────────────
    private void refreshTable() {

        if (currentCourseId == -1 || currentDate == null) return;

        attModel.setRowCount(0);
        int count = 0;

        try {
            pst = con.prepareStatement(
                "SELECT s.name, a.status, a.created_at " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "WHERE a.course_id=? AND a.attendance_date=? " +
                "ORDER BY a.created_at DESC");
            pst.setInt(1, currentCourseId);
            pst.setString(2, currentDate);
            rs = pst.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("a.created_at");
                String time  = ts != null
                    ? new SimpleDateFormat("HH:mm:ss").format(ts)
                    : "—";

                attModel.addRow(new Object[]{
                    ++count,
                    rs.getString("s.name"),
                    rs.getString("a.status"),
                    time
                });
            }

            lblScanCount.setText(count + " student" +
                (count == 1 ? "" : "s") + " scanned");

            TableUtils.resizeColumnWidth(attTable);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  SAVE QR IMAGE
    // ─────────────────────────────────────────
    private void saveQRImage() {

        if (lblQRImage.getIcon() == null) {
            JOptionPane.showMessageDialog(this,
                "Generate a QR code first.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save QR Code Image");
        Course course = (Course) courseBox.getSelectedItem();
        String name = course != null
            ? course.getCourseName().replace(" ", "_")
                + "_QR_" + currentDate + ".png"
            : "QR_Code.png";
        fc.setSelectedFile(new File(name));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".png"))
            file = new File(file.getAbsolutePath() + ".png");

        try {
            ImageIcon icon = (ImageIcon) lblQRImage.getIcon();
            BufferedImage img = new BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_RGB);
            Graphics g = img.createGraphics();
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();
            ImageIO.write(img, "PNG", file);
            JOptionPane.showMessageDialog(this,
                "QR saved: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Save failed: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private String getSelectedDate() {
        java.util.Date d = (java.util.Date) datePicker.getValue();
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private void setFeedback(String msg, Color color) {
        lblScanFeedback.setText(msg);
        lblScanFeedback.setForeground(color);
    }
}