package view;

import controller.gameHistoryController;
import model.gameHistory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class gameHistoryView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JButton btnBack;

    public gameHistoryView(gameHistoryController controller) {

        setTitle("MineSweeper- History");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        ImageIcon originalIcon;
        try {
            originalIcon = new ImageIcon(getClass().getResource("/images/background.jpeg"));
        } catch (Exception e) {
            originalIcon = new ImageIcon("src/images/background.jpeg");
        }

        Image scaledImage = originalIcon.getImage()
                .getScaledInstance(900, 600, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel bg = new JLabel(scaledIcon);
        bg.setBounds(0, 0, 900, 600);
        bg.setLayout(null);
        add(bg);

        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        getLayeredPane().add(speaker, JLayeredPane.POPUP_LAYER);

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 40;

        speaker.setBounds(
                marginLeft,
                getHeight() - iconSize - marginBottom,
                iconSize,
                iconSize
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setLocation(
                        marginLeft,
                        getHeight() - iconSize - marginBottom
                );
            }
        });

        JLabel title = new JLabel("Games History", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 44));
        title.setBounds(0, 60, 900, 50);
        bg.add(title);

        JLabel subtitle = new JLabel(
                "See your wins, losses, and progress over time",
                SwingConstants.CENTER
        );
        subtitle.setForeground(new Color(240, 240, 240));
        subtitle.setFont(new Font("Serif", Font.PLAIN, 20));
        subtitle.setBounds(0, 110, 900, 30);
        bg.add(subtitle);

        btnBack = new JButton("← Go Back") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 18;

                // background
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                // border
                g2.setColor(new Color(200, 200, 200, 220));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setBounds(710, 35, 160, 40);
        btnBack.setFont(new Font("Serif", Font.BOLD, 16));
        btnBack.setForeground(Color.BLACK);
        btnBack.setFocusPainted(false);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setOpaque(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bg.add(btnBack);
        btnBack.addActionListener(e -> dispose());

        // ===== WHITE GLASS PANEL =====
        Color glass = new Color(255, 255, 255, 215);

        int panelWidth = 760;
        int panelHeight = 300;
        int panelX = (900 - panelWidth) / 2;
        int panelY = 175;

        RoundedPanel overlayPanel = new RoundedPanel(22, glass);
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        overlayPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        bg.add(overlayPanel);

        String[] columnNames = {"Date", "Player A", "Player B", "Result", "Time", "Score", "Level"};

        int rows = controller.getNumberOfEntries();
        Object[][] data = new Object[rows][columnNames.length];

        for (int i = 0; i < rows; i++) {
            gameHistory entry = controller.getEntry(i);
            data[i][0] = entry.getDate();
            data[i][1] = entry.getPlayerA();
            data[i][2] = entry.getPlayerB();
            data[i][3] = entry.getResult();
            data[i][4] = entry.getDuration();
            data[i][5] = entry.getScore();
            data[i][6] = entry.getLevel();
        }

        JTable historyTable = new JTable(data, columnNames);
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
        scrollPane.getViewport().setOpaque(true);

        overlayPanel.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}
