package src;

import src.forms.master.CourseForm;
import src.forms.master.TeacherForm;
import src.forms.transaction.CourseSelectionForm;
import src.forms.transaction.FeesReceiptForm;
import src.forms.transaction.RegistrationForm;
import src.forms.transaction.StudentEnquiryForm;

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
        master.add(createMenuItem("Course", () -> new CourseForm()));
        master.add(createMenuItem("Teacher", () -> new TeacherForm()));
        menuBar.add(master);

        // TRANSACTION
        JMenu transaction = createMenu("Transaction");
        transaction.add(createMenuItem("Student Registration", () -> new RegistrationForm()));
        transaction.add(createMenuItem("Course Enrollment", () -> new CourseSelectionForm()));
        transaction.add(createMenuItem("Fees Receipt", () -> new FeesReceiptForm()));
        transaction.add(createMenuItem("Student Enquiry", () -> new StudentEnquiryForm()));
        menuBar.add(transaction);

        // REPORT
        JMenu report = createMenu("Report");
        report.add(createMenuItem("Coming Soon",
                () -> JOptionPane.showMessageDialog(this, "Reports Coming Soon")));
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
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        cardsPanel.setBackground(BACKGROUND);
        cardsPanel.setMaximumSize(new Dimension(700, 300));

        cardsPanel.add(createCard(
                "Student Registration",
                "Register new students",
                () -> new RegistrationForm()));

        cardsPanel.add(createCard(
                "Course Enrollment",
                "Enroll students in courses",
                () -> new CourseSelectionForm()));

        cardsPanel.add(createCard(
                "Fees Receipt",
                "Manage fee payments",
                () -> new FeesReceiptForm()));

        cardsPanel.add(createCard(
                "Student Enquiry",
                "Handle student enquiries",
                () -> new StudentEnquiryForm()));

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
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(Color.GRAY);

        JButton openBtn = new JButton("Open");
        openBtn.setFocusPainted(false);
        openBtn.setBackground(ACCENT);
        openBtn.setForeground(Color.WHITE);
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        openBtn.addActionListener(e -> action.run());

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(openBtn);

        return card;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(MainMenu::new);
    }
}