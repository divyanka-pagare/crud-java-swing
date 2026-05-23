package src.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable() {

        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        setRowHeight(40);

        setShowGrid(false);

        setIntercellSpacing(new Dimension(0,0));

        setSelectionBackground(new Color(232,240,254));

        setBackground(Color.WHITE);

        setForeground(new Color(40,40,40));

        getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        getTableHeader().setBackground(
                new Color(22,34,57)
        );

        getTableHeader().setForeground(Color.WHITE);

        getTableHeader().setPreferredSize(
                new Dimension(100,45)
        );

        setDefaultRenderer(
                Object.class,
                new ModernTableCellRenderer()
        );
    }

    static class ModernTableCellRenderer
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
                            0,10,0,10
                    )
            );

            if(isSelected){

                c.setBackground(
                        new Color(232,240,254)
                );

            } else {

                c.setBackground(
                        row % 2 == 0
                                ? Color.WHITE
                                : new Color(245,247,250)
                );
            }

            return c;
        }
    }
}