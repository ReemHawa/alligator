package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

  
	private static final long serialVersionUID = 1L;
	private Color normalColor = new Color(190, 240, 230);   // mint
    private Color hoverColor  = new Color(170, 225, 215);   // a bit darker
    private boolean hovered = false;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(new Font("Serif", Font.BOLD, 16));
        setForeground(Color.DARK_GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = getHeight(); // makes it pill-shaped
        g2.setColor(hovered ? hoverColor : normalColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
