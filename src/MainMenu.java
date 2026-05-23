package src;

import src.forms.master.CourseForm;
import src.forms.master.TeacherForm;
import src.forms.transaction.RegistrationForm;
import src.forms.transaction.CourseSelectionForm;
import src.forms.transaction.FeesReceiptForm;
import src.forms.transaction.StudentEnquiryForm;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {

        setTitle("Student Management System");
        setSize(1200, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Menu Bar ──
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 40, 55));
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        // ── Master Menu ──
        JMenu master = menu("Master");
        master.add(menuItem("Course",  () -> new CourseForm()));
        master.add(menuItem("Teacher", () -> new TeacherForm()));
        menuBar.add(master);

        // ── Transaction Menu ──
        JMenu transaction = menu("Transaction");
        transaction.add(menuItem("Student Registration", () -> new RegistrationForm()));
        transaction.add(menuItem("Course Enrollment",    () -> new CourseSelectionForm()));
        transaction.add(menuItem("Fees Receipt",         () -> new FeesReceiptForm()));
        transaction.add(menuItem("Student Enquiry",      () -> new StudentEnquiryForm()));
        menuBar.add(transaction);

        // ── Report Menu ──
        JMenu report = menu("Report");
        report.add(menuItem("Coming Soon", () ->
            JOptionPane.showMessageDialog(this, "Reports coming soon!")));
        menuBar.add(report);

        // ── Settings Menu ──
        JMenu settings = menu("Settings");
        settings.add(menuItem("Coming Soon", () ->
            JOptionPane.showMessageDialog(this, "Settings coming soon!")));
        menuBar.add(settings);

        // ── Help Menu ──
        JMenu help = menu("Help");
        help.add(menuItem("About", () ->
            JOptionPane.showMessageDialog(this,
                "Student Management System\n" +
                "Version 1.0\n" +
                "Developed by: " + System.getProperty("user.name"),
                "About", JOptionPane.INFORMATION_MESSAGE)));
        menuBar.add(help);

        setJMenuBar(menuBar);

        // ── Welcome Panel ──
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 247, 250));

        JLabel welcome = new JLabel("Welcome to Student Management System");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcome.setForeground(new Color(30, 40, 55));
        welcome.setBounds(0, 250, 1200, 50);
        welcome.setHorizontalAlignment(JLabel.CENTER);
        panel.add(welcome);

        JLabel sub = new JLabel("Use the menu bar above to navigate");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 100, 110));
        sub.setBounds(0, 310, 1200, 30);
        sub.setHorizontalAlignment(JLabel.CENTER);
        panel.add(sub);

        add(panel);

        setVisible(true);
    }

    private JMenu menu(String text) {
        JMenu m = new JMenu(text);
        m.setFont(new Font("Segoe UI", Font.BOLD, 13));
        m.setForeground(Color.WHITE);
        m.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return m;
    }

    private JMenuItem menuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.addActionListener(e -> action.run());
        return item;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}