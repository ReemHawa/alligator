package view;

import javax.swing.*;
import java.awt.*;
import controller.homeScreenController;

public class HomeScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnStartNewGame;
    private JButton btnViewHistory;
    private JButton btnViewQuestions;

    public HomeScreen() {

        // music.bcMusic.play("/music/Host Entrance Background Music.wav");

        setTitle("MineSweeper - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        setSize(800, 550);
        setLocationRelativeTo(null);

        // ===== Speaker icon (bottom-left) =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        bg.add(speaker);

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 5;

        speaker.setBounds(
                marginLeft,
                getContentPane().getHeight() - iconSize - marginBottom,
                iconSize,
                iconSize
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setLocation(
                        marginLeft,
                        getContentPane().getHeight() - iconSize - marginBottom
                );
            }
        });

        // ===== Title =====
        JLabel title = new JLabel("Welcome To Minesweeper", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setBounds(200, 60, 400, 40);
        bg.add(title);

        JLabel sub = new JLabel("Start the game when you're ready :)", SwingConstants.CENTER);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font("Serif", Font.PLAIN, 16));
        sub.setBounds(230, 100, 340, 30);
        bg.add(sub);

        // ===== Buttons (same size as before) =====
        btnViewHistory = new GlassButton("View Games History");
        btnViewHistory.setBounds(270, 190, 260, 40);
        bg.add(btnViewHistory);

        btnViewQuestions = new GlassButton("View Questions Management");
        btnViewQuestions.setBounds(270, 250, 260, 40);
        bg.add(btnViewQuestions);

        btnStartNewGame = new GlassButton("Start A New Game");
        btnStartNewGame.setBounds(270, 310, 260, 40);
        bg.add(btnStartNewGame);

        new homeScreenController(this);

        setVisible(true);
    }

    public JButton getBtnStartNewGame() { return btnStartNewGame; }
    public JButton getBtnViewHistory() { return btnViewHistory; }
    public JButton getBtnViewQuestions() { return btnViewQuestions; }

    // ==========================================================
    // Custom dark glass-style button (same style, old colors)
    // ==========================================================
    private static class GlassButton extends JButton {

        private static final long serialVersionUID = 1L;

        public GlassButton(String text) {
            super(text);

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setForeground(Color.WHITE);
            setFont(new Font("Serif", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18;

            // background (dark, semi-transparent)
            g2.setColor(new Color(30, 30, 30, 200));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // border
            g2.setColor(new Color(220, 220, 220, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    arc, arc);

            // hover overlay
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ==========================================================
    // Background panel
    // ==========================================================
    private static class BackgroundPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon(
                        getClass().getResource("/images/background.jpeg")
                ).getImage();
            } catch (Exception e) {
                backgroundImage = new ImageIcon("src/images/background.jpeg").getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
