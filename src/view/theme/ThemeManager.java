package view.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ThemeManager {

    private ThemeMode mode = ThemeMode.LIGHT;

    // צבעים כלליים
    public Color bg() { return mode == ThemeMode.DARK ? new Color(25, 25, 28) : Color.WHITE; }
    public Color panelBg() { return mode == ThemeMode.DARK ? new Color(35, 35, 40) : new Color(245, 245, 245); }
    public Color text() { return mode == ThemeMode.DARK ? new Color(230, 230, 235) : Color.BLACK; }

    // צבעים ללוח
    public Color cellClosedBg() { return mode == ThemeMode.DARK ? new Color(55, 55, 62) : new Color(210, 210, 210); }
    public Color cellOpenBg()   { return mode == ThemeMode.DARK ? new Color(40, 40, 46) : new Color(240, 240, 240); }
    public Color cellBorder()   { return mode == ThemeMode.DARK ? new Color(90, 90, 100) : new Color(160, 160, 160); }

    public ThemeMode getMode() { return mode; }
    public void setMode(ThemeMode m) {
        this.mode = m;
    }


    public void toggle() {
        mode = (mode == ThemeMode.LIGHT) ? ThemeMode.DARK : ThemeMode.LIGHT;
    }
    
    
    public Color boardBorder() {
        return mode == ThemeMode.DARK
                ? new Color(255, 200, 0)   // צהוב חמים
                : new Color(210, 160, 0);
    }

    public Color numberColor(int n) {
        switch (n) {
            case 1: return mode == ThemeMode.DARK ? new Color(120,180,255) : new Color(0,70,200);
            case 2: return mode == ThemeMode.DARK ? new Color(120,255,150) : new Color(0,140,60);
            case 3: return mode == ThemeMode.DARK ? new Color(255,140,140) : new Color(200,40,40);
            case 4: return mode == ThemeMode.DARK ? new Color(170,140,255) : new Color(90,60,180);
            case 5: return mode == ThemeMode.DARK ? new Color(255,200,120) : new Color(180,90,0);
            default: return text();
        }
    }

    

    /** מיישם צבעים בסיסיים על קומפוננטה וכל הילדים שלה */
    public void applyTo(Component root) {
        applyRecursive(root);

        if (root instanceof JComponent) {
            JComponent jc = (JComponent) root;
            jc.revalidate();
            jc.repaint();
        }
    }

    private void applyRecursive(Component c) {

        if (c instanceof JPanel) {
            JPanel p = (JPanel) c;
            p.setBackground(panelBg());
            p.setForeground(text());

        } else if (c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setForeground(text());

        } else if (c instanceof JButton) {
            JButton b = (JButton) c;

            Object isCell = b.getClientProperty("isCell");
            if (Boolean.TRUE.equals(isCell)) {
                // תא בלוח – לא נוגעים ברקע כדי לא לפגוע באייקונים
                if (b.getIcon() == null) {
                    b.setForeground(text());
                }
            } else {
                // כפתורי UI רגילים
                b.setBackground(panelBg());
                b.setForeground(text());
            }

        } else {
            c.setBackground(bg());
            c.setForeground(text());
        }

        if (c instanceof Container) {
            Container container = (Container) c;
            for (Component child : container.getComponents()) {
                applyRecursive(child);
            }
        }
    }

}
