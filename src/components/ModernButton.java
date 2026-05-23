package src.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    private Color backgroundColor;
    private boolean hovered = false;
    private boolean pressed = false;

    public ModernButton(String text, Color color) {

        super(text);

        this.backgroundColor = color;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);

        setForeground(Color.WHITE);

        setFont(new Font("Segoe UI", Font.BOLD, 15));

        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setOpaque(false);

        // Smooth Padding Feel
        setMargin(new Insets(12, 22, 12, 22));

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int shadowOffset = hovered ? 4 : 2;

        // Soft Shadow
        g2.setColor(new Color(0, 0, 0, hovered ? 40 : 20));
        g2.fillRoundRect(
                shadowOffset,
                shadowOffset,
                getWidth() - shadowOffset,
                getHeight() - shadowOffset,
                12,
                12
        );

        // Button Background
        Color bg = backgroundColor;

        if (pressed) {
            bg = backgroundColor.darker();
        } else if (hovered) {
            bg = new Color(
                    Math.min(backgroundColor.getRed() + 15, 255),
                    Math.min(backgroundColor.getGreen() + 15, 255),
                    Math.min(backgroundColor.getBlue() + 15, 255)
            );
        }

        g2.setColor(bg);

        g2.fillRoundRect(
                0,
                0,
                getWidth() - 4,
                getHeight() - 4,
                18,
                18
        );

        g2.dispose();

        super.paintComponent(g);
    }
}