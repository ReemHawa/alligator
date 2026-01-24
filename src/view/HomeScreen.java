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
        setTitle("MineSweeper - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        // ✅ IMPORTANT: use layout manager (not null)
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ===== Center content (title + subtitle + buttons) =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome To Minesweeper", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 34));

        JLabel sub = new JLabel("Start the game when you're ready :)", SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setForeground(Color.WHITE);
        sub.setFont(new Font("Serif", Font.PLAIN, 16));

        center.add(title);
        center.add(Box.createVerticalStrut(8));
        center.add(sub);
        center.add(Box.createVerticalStrut(45));

        btnViewHistory = createWideButton("View Games History");
        btnViewQuestions = createWideButton("View Questions Management");
        btnStartNewGame = createWideButton("Start A New Game");

        center.add(btnViewHistory);
        center.add(Box.createVerticalStrut(18));
        center.add(btnViewQuestions);
        center.add(Box.createVerticalStrut(18));
        center.add(btnStartNewGame);

        // ✅ Add center panel, always centered
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 0;
        gbcCenter.gridy = 0;
        gbcCenter.weightx = 1;
        gbcCenter.weighty = 1;
        gbcCenter.anchor = GridBagConstraints.CENTER;
        gbcCenter.fill = GridBagConstraints.NONE;
        bg.add(center, gbcCenter);

        // ===== Speaker icon (always bottom-left) =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        int iconSize = 40;
        speaker.setPreferredSize(new Dimension(iconSize, iconSize));

        GridBagConstraints gbcSpeaker = new GridBagConstraints();
        gbcSpeaker.gridx = 0;
        gbcSpeaker.gridy = 1;
        gbcSpeaker.weightx = 1;
        gbcSpeaker.weighty = 0;
        gbcSpeaker.anchor = GridBagConstraints.SOUTHWEST;
        gbcSpeaker.insets = new Insets(0, 10, 8, 0);
        bg.add(speaker, gbcSpeaker);

        new homeScreenController(this);

        // ✅ Better resize behavior
        setMinimumSize(new Dimension(800, 550));
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createWideButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ✅ fixed nice size; stays centered on resize
        b.setPreferredSize(new Dimension(320, 45));
        b.setMaximumSize(new Dimension(320, 45));
        b.setMinimumSize(new Dimension(320, 45));
        return b;
    }

    public JButton getBtnStartNewGame() { return btnStartNewGame; }
    public JButton getBtnViewHistory() { return btnViewHistory; }
    public JButton getBtnViewQuestions() { return btnViewQuestions; }

    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon(
                        getClass().getResource("/images/background.jpeg")
                ).getImage();
            } catch (Exception e) {
                // Fallback for IDE run (not JAR)
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
