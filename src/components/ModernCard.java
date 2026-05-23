package src.components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ModernCard extends JPanel {

    public ModernCard(String title) {

        setLayout(null);

        setBackground(Color.WHITE);

        setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(
                    new Color(210,215,220)
                ),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12)
            )
        );
    }
}