package src.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ModernTable extends JTable {

    private int hoveredRow = -1;

    public ModernTable() {

        // ===== TABLE DESIGN =====

        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        setRowHeight(40);

        setShowGrid(false);

        setIntercellSpacing(new Dimension(0, 0));

        setFocusable(false);

        setSelectionBackground(new Color(220, 235, 252));

        setSelectionForeground(Color.BLACK);

        setBackground(Color.WHITE);

        setForeground(new Color(40, 40, 40));

        // ===== HEADER DESIGN =====

        getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        getTableHeader().setBackground(
                new Color(15, 23, 42)
        );

        getTableHeader().setForeground(Color.WHITE);

        getTableHeader().setPreferredSize(
                new Dimension(100, 45)
        );

        getTableHeader().setReorderingAllowed(false);

        getTableHeader().setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        1,
                        0,
                        new Color(35, 45, 65)
                )
        );

        // ===== RENDERER =====

        setDefaultRenderer(
                Object.class,
                new ModernTableCellRenderer()
        );

        // ===== ROW HOVER EFFECT =====

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {

                hoveredRow = rowAtPoint(e.getPoint());

                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {

                hoveredRow = -1;

                repaint();
            }
        });
    }

    // ===== CUSTOM CELL RENDERER =====

    class ModernTableCellRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {

            Component c =
                    super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            setBorder(
                    BorderFactory.createEmptyBorder(
                            0,
                            12,
                            0,
                            12
                    )
            );

            // ===== ROW COLORS =====

            if (isSelected) {

                c.setBackground(new Color(220, 235, 252));

                c.setForeground(Color.BLACK);

            } else if (row == hoveredRow) {

                c.setBackground(new Color(242, 247, 255));

                c.setForeground(Color.BLACK);

            } else {

                c.setBackground(
                        row % 2 == 0
                                ? Color.WHITE
                                : new Color(248, 250, 252)
                );

                c.setForeground(new Color(50, 50, 50));
            }

            return c;
        }
    }
}