package view;

import controller.gameHistoryController;
import model.gameHistory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class gameHistoryView extends JFrame {

    private static final long serialVersionUID = 1L;

    public gameHistoryView(gameHistoryController controller) {

        setTitle("MineSweeper - History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ===== TOP-RIGHT Back =====
        JButton btnBack = new JButton("\u2190 Go Back");
        btnBack.setFocusPainted(false);

        // ✅ make it SAME style as HomeScreen buttons
        styleButton(btnBack);

        GridBagConstraints gbcBack = new GridBagConstraints();
        gbcBack.gridx = 0;
        gbcBack.gridy = 0;
        gbcBack.weightx = 1;
        gbcBack.anchor = GridBagConstraints.NORTHEAST;
        gbcBack.insets = new Insets(18, 18, 10, 18);
        bg.add(btnBack, gbcBack);

        btnBack.addActionListener(e -> dispose());

        // ===== CENTER Card =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Games History", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 44));

        JLabel subtitle = new JLabel("See your wins, losses, and progress over time", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(new Color(240, 240, 240));
        subtitle.setFont(new Font("Serif", Font.PLAIN, 20));

        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(subtitle);
        center.add(Box.createVerticalStrut(18));

        // ===== Table model =====
        String[] columnNames = {"Date", "Player A", "Player B", "Result", "Time", "Score", "Level"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < controller.getNumberOfEntries(); i++) {
            gameHistory entry = controller.getEntry(i);
            tableModel.addRow(new Object[]{
                    entry.getDate(),
                    entry.getPlayerA(),
                    entry.getPlayerB(),
                    entry.getResult(),
                    entry.getDuration(),
                    entry.getScore(),
                    entry.getLevel()
            });
        }

        JTable historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Serif", Font.PLAIN, 18));
        historyTable.setRowHeight(34);
        historyTable.setGridColor(new Color(225, 225, 225));
        historyTable.setBackground(Color.WHITE);
        historyTable.setForeground(Color.BLACK);
        historyTable.setFillsViewportHeight(true);

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Serif", Font.BOLD, 18));
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        int[] columnWidths = {120, 120, 120, 90, 110, 90, 100};
        for (int i = 0; i < columnWidths.length && i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        RoundedPanel overlayPanel = new RoundedPanel(22, new Color(255, 255, 255, 215));
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        overlayPanel.add(scrollPane, BorderLayout.CENTER);

        overlayPanel.setPreferredSize(new Dimension(780, 330));
        overlayPanel.setMaximumSize(new Dimension(1050, 450));

        center.add(overlayPanel);

        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 1;
        gbcCenter.weightx = 1;
        gbcCenter.weighty = 1;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        gbcCenter.insets = new Insets(0, 25, 25, 25);
        bg.add(center, gbcCenter);

        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        speaker.setPreferredSize(new Dimension(40, 40));

        GridBagConstraints gbcSpeaker = new GridBagConstraints();
        gbcSpeaker.gridx = 0;
        gbcSpeaker.gridy = 2;
        gbcSpeaker.weightx = 1;
        gbcSpeaker.anchor = GridBagConstraints.SOUTHWEST;
        gbcSpeaker.insets = new Insets(0, 10, 8, 0);
        bg.add(speaker, gbcSpeaker);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ✅ SAME button style as HomeScreen
    private void styleButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);

        if (b.getBackground() == null || b.getBackground().equals(new JButton().getBackground())) {
            b.setBackground(new Color(255, 255, 255, 235));
        }
        if (b.getForeground() == null || b.getForeground().equals(new JButton().getForeground())) {
            b.setForeground(Color.BLACK);
        }

        b.setFont(new Font("Serif", Font.BOLD, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 16, 8, 16));

        final Color normalBg = b.getBackground();
        final Color hoverBg = new Color(
                normalBg.getRed(),
                normalBg.getGreen(),
                normalBg.getBlue(),
                Math.min(255, normalBg.getAlpha() + 18)
        );

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hoverBg); b.repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { b.setBackground(normalBg); b.repaint(); }
        });

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton btn = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 18;

                g2.setColor(btn.getBackground());
                g2.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), arc, arc);

                g2.setColor(new Color(200, 200, 200, 220));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, btn.getWidth() - 3, btn.getHeight() - 3, arc, arc);

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image bg;

        public BackgroundPanel() {
            try { bg = new ImageIcon(getClass().getResource("/images/background.jpeg")).getImage(); }
            catch (Exception e) { bg = new ImageIcon("src/images/background.jpeg").getImage(); }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
