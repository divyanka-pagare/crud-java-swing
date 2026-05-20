package src;

import src.forms.RegistrationForm;
import src.forms.FeesReceiptForm;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame menu = new JFrame("Student Management System");

            menu.setSize(420, 320);
            menu.setLayout(null);
            menu.setLocationRelativeTo(null);
            menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // ===== Registration Button =====
            JButton btnRegistration =
                    new JButton("Student Registration");

            btnRegistration.setFont(
                    new Font("Segoe UI", Font.BOLD, 14));

            btnRegistration.setBounds(50, 100, 300, 45);

            btnRegistration.setBackground(
                    new Color(0, 120, 215));

            btnRegistration.setForeground(Color.WHITE);

            menu.add(btnRegistration);

            // ===== Fees Receipt Button =====
            JButton btnFees =
                    new JButton("Fees Receipt");

            btnFees.setFont(
                    new Font("Segoe UI", Font.BOLD, 14));

            btnFees.setBackground(
                    new Color(153, 0, 153));

            btnFees.setForeground(Color.WHITE);

            btnFees.setBounds(50, 170, 300, 45);

            menu.add(btnFees);

            // ===== BUTTON ACTIONS =====

            btnRegistration.addActionListener(
                    e -> new RegistrationForm());

            btnFees.addActionListener(
                    e -> new FeesReceiptForm());

            menu.setVisible(true);
        });
    }
}