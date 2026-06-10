package src;

import src.forms.attendance.AttendanceForm;
import src.forms.attendance.AttendanceReport;
import src.forms.attendance.TodayAttendanceLog;
import src.forms.master.CourseForm;
import src.forms.master.TeacherForm;
import src.forms.transaction.CourseSelectionForm;
import src.forms.transaction.FeesReceiptForm;
import src.forms.transaction.RegistrationForm;
import src.forms.transaction.StudentEnquiryForm;
import src.forms.report.FeesCollectionReport;
import src.forms.attendance.QRAttendanceForm;

import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainMenu extends JFrame {

    private final Color PRIMARY = new Color(22, 34, 57);
    private final Color ACCENT = new Color(0, 173, 181);
    private final Color BACKGROUND = new Color(245, 247, 250);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 40);
    private final Font SUB_FONT = new Font("Segoe UI", Font.PLAIN, 20);

    public MainMenu() {

        // ───────────────── FRAME ─────────────────
        setTitle("Student Management System");
        setSize(1400, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ───────────────── MENU BAR ─────────────────
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY);
        menuBar.setBorder(new EmptyBorder(8, 15, 8, 15));

        // MASTER
        JMenu master = createMenu("Master");
        master.add(createMenuItem("Course", () -> UIUtils.openFullScreen(new CourseForm())));
        master.add(createMenuItem("Teacher", () -> UIUtils.openFullScreen(new TeacherForm())));
        menuBar.add(master);

        // TRANSACTION
        JMenu transaction = createMenu("Transaction");
        transaction.add(createMenuItem("Student Registration",  () -> UIUtils.openFullScreen(new RegistrationForm())));
        transaction.add(createMenuItem("Course Enrollment",     () -> UIUtils.openFullScreen(new CourseSelectionForm())));
        transaction.add(createMenuItem("Fees Receipt",          () -> UIUtils.openFullScreen(new FeesReceiptForm())));
        transaction.add(createMenuItem("Student Enquiry",       () -> UIUtils.openFullScreen(new StudentEnquiryForm())));
        menuBar.add(transaction);

        // ATTENDANCE
        JMenu attendance = createMenu("Attendance");
        
        attendance.add(createMenuItem("Mark Attendance",           () -> UIUtils.openFullScreen(new AttendanceForm())));
        attendance.add(createMenuItem("Today's Log",               () -> UIUtils.openFullScreen(new TodayAttendanceLog())));      
        attendance.add(createMenuItem("Attendance Report",         () -> UIUtils.openFullScreen(new AttendanceReport())));
        attendance.add(createMenuItem("QR Attendance",        () -> UIUtils.openFullScreen(new QRAttendanceForm())));
        // attendance.add(createMenuItem("Monthly Attendance Report",   () -> UIUtils.openFullScreen(new MonthlyAttendanceReportForm())));
        // attendance.add(createMenuItem("Attendance Percentage",       () -> UIUtils.openFullScreen(new AttendancePercentageForm())));
        // attendance.add(createMenuItem("Defaulter List",              () -> UIUtils.openFullScreen(new DefaulterListForm())));
        // attendance.add(createMenuItem("Course-wise Attendance",      () -> UIUtils.openFullScreen(new CourseWiseAttendanceForm())));
        // attendance.add(createMenuItem("Teacher-wise Attendance",     () -> UIUtils.openFullScreen(new TeacherWiseAttendanceForm())));
        // attendance.add(createMenuItem("Export Attendance",           () -> UIUtils.openFullScreen(new ExportAttendanceForm())));
        // attendance.add(createMenuItem("Dashboard Statistics",        () -> UIUtils.openFullScreen(new AttendanceDashboardForm())));
        menuBar.add(attendance);
        
        

        // REPORT
        JMenu report = createMenu("Report");
        report.add(createMenuItem("Fees Collection Report",
                () -> UIUtils.openFullScreen(new FeesCollectionReport())));
        menuBar.add(report);

        // SETTINGS
        JMenu settings = createMenu("Settings");
        settings.add(createMenuItem("Coming Soon",
                () -> JOptionPane.showMessageDialog(this, "Settings Coming Soon")));
        menuBar.add(settings);

        // HELP
        JMenu help = createMenu("Help");
        help.add(createMenuItem("About", () ->
                JOptionPane.showMessageDialog(this,
                        "Student Management System\nVersion 1.0",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE)));
        menuBar.add(help);

        setJMenuBar(menuBar);

        // ───────────────── MAIN PANEL ─────────────────
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);

        // ───────────────── TOP HEADER ─────────────────
        JPanel header = new JPanel();
        header.setBackground(ACCENT);
        header.setPreferredSize(new Dimension(100, 80));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20));

        JLabel heading = new JLabel("Student Management Dashboard");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(Color.WHITE);

        header.add(heading);

        // ───────────────── CENTER PANEL ─────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(BACKGROUND);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(50, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome Back!");
        welcome.setFont(TITLE_FONT);
        welcome.setForeground(PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage students, courses, fees and reports efficiently.");
        subtitle.setFont(SUB_FONT);
        subtitle.setForeground(new Color(90, 90, 90));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(welcome);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(subtitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // ───────────────── QUICK ACTION CARDS ─────────────────
        JPanel cardsPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 30, 20)
        );
        cardsPanel.setBackground(BACKGROUND);

        cardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardsPanel.add(createCard(
                "Student Registration",
                "Register new students",
                () -> new RegistrationForm()));

        cardsPanel.add(createCard(
                "Student Attendance",
                "Manage student attendance",
                () -> new AttendanceForm()));

        centerPanel.add(cardsPanel);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        setVisible(true);
    }

    // ───────────────── MENU ─────────────────
    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);

        menu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menu.setForeground(Color.WHITE);

        return menu;
    }

    // ───────────────── MENU ITEM ─────────────────
    private JMenuItem createMenuItem(String text, Runnable action) {

        JMenuItem item = new JMenuItem(text);

        item.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        item.setPreferredSize(new Dimension(220, 35));

        item.addActionListener(e -> action.run());

        return item;
    }

    // ───────────────── DASHBOARD CARD ─────────────────
    private JPanel createCard(String title, String subtitle, Runnable action) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 15, 12, 15)
        ));

        card.setPreferredSize(new Dimension(240, 140)); 
        card.setMaximumSize(new Dimension(240, 140));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(Color.GRAY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);


        JButton openBtn = new JButton("Open");
        openBtn.setFocusPainted(false);
        openBtn.setBackground(ACCENT);
        openBtn.setForeground(Color.WHITE);
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        openBtn.setPreferredSize(new Dimension(80, 28));

        openBtn.addActionListener(e -> action.run());

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(openBtn);

        return card;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(MainMenu::new);
    }
}