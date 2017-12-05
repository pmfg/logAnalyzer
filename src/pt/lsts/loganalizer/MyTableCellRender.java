package pt.lsts.loganalizer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

class MyTableCellRender extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    private Color COLOR_ERROR = new Color(250, 100, 100);
    private Color COLOR_WARMING = new Color(230, 190, 80);
    private Color COLOR_CRITICAL = new Color(170, 80, 230);

    public MyTableCellRender() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        String text = (String) value;
        if (column == 0) {
            if (text.equals("ERROR")) {
                setForeground(Color.black);
                setBackground(COLOR_ERROR);
            }
            else if (text.equals("WARMING")) {
                setForeground(Color.black);
                setBackground(COLOR_WARMING);
            }
            else if (text.equals("CRITICAL")) {
                setForeground(Color.black);
                setBackground(COLOR_CRITICAL);
            }
        }
        setText(value != null ? value.toString() : "");

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            Color c = Color.CYAN;
            label.setBackground(c);
        }

        return this;
    }
}