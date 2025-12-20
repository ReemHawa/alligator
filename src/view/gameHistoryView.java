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

        setTitle("MineSweeper - History");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // ===== BACKGROUND (correct, safe loading!) =====
        ImageIcon originalIcon;
        try {
            originalIcon = new ImageIcon(
                    getClass().getResource("/images/background.jpeg")   // classpath load
            );
        } catch (Exception e) {
            originalIcon = new ImageIcon("src/images/background.jpeg"); // fallback
        }

        Image scaledImage = originalIcon.getImage()
                .getScaledInstance(800, 550, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel bg = new JLabel(scaledIcon);
        bg.setBounds(0, 0, 800, 550);
        bg.setLayout(null);
        add(bg);

        //add speaker / mute feature
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


        
        // ===== TITLE =====
        JLabel title = new JLabel("Games History", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setBounds(200, 50, 400, 40);
        bg.add(title);

        // ===== SUBTITLE =====
        JLabel subtitle = new JLabel(
                "See your wins, losses, and progress over time",
                SwingConstants.CENTER
        );
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Serif", Font.PLAIN, 18));
        subtitle.setBounds(140, 90, 520, 30);
        bg.add(subtitle);

        // ===== BACK BUTTON =====
        btnBack = new RoundedButton("← Go Back");
        btnBack.setBounds(640, 20, 130, 40);
        bg.add(btnBack);
        btnBack.addActionListener(e -> dispose());

        // ===== OVERLAY PANEL FOR TABLE =====
        Color mintColor = new Color(190, 240, 230, 210); // semi-transparent mint

        int panelWidth = 650;
        int panelHeight = 220;
        int panelX = (800 - panelWidth) / 2;
        int panelY = 150;

        RoundedPanel overlayPanel = new RoundedPanel(25, mintColor);
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        overlayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bg.add(overlayPanel);

        // ===== BUILD TABLE FROM CONTROLLER =====
        String[] columnNames = {
                "Date", "Player A", "Player B",
                "Result", "Time", "Score", "Level"
        };

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

        // table styling
        Font cellFont = new Font("Serif", Font.PLAIN, 18);
        historyTable.setFont(cellFont);
        historyTable.setRowHeight(30);
        historyTable.setGridColor(new Color(160, 200, 190));

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Serif", Font.BOLD, 18));
        header.setBackground(new Color(180, 230, 220, 230));
        header.setForeground(Color.DARK_GRAY);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setOpaque(false);

        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        int[] columnWidths = {110, 100, 100, 70, 95, 70, 85};
        for (int i = 0; i < columnWidths.length && i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        historyTable.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        overlayPanel.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}
