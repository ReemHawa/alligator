package view;

import javax.swing.*;
import java.awt.*;

public class PlayersNamesScreen extends JFrame {

    private static final long serialVersionUID = 1L;
	private JTextField txtPlayerA;
    private JTextField txtPlayerB;
    private JButton btnPlay;
    private JButton btnBack;

    private String level;
    private chooseLevelView levelScreen;
    private HomeScreen homeScreen;

    public PlayersNamesScreen(String level, chooseLevelView levelScreen, HomeScreen homeScreen) {
        this.level = level;
        this.levelScreen = levelScreen;
        this.homeScreen = homeScreen;

        setTitle("MineSweepesr_Players");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🔥 Add background panel
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ---------- TITLE ----------
        JLabel lblTitle = new JLabel("Enter your names:");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(200, 120, 400, 40);
        bg.add(lblTitle);

        // ---------- INPUT FIELDS ----------
        txtPlayerA = new JTextField();
        txtPlayerA.setBounds(260, 220, 280, 48);
        bg.add(txtPlayerA);

        txtPlayerB = new JTextField();
        txtPlayerB.setBounds(260, 280, 280, 48);
        bg.add(txtPlayerB);

        // ---------- PLAY BUTTON ----------
        btnPlay = new JButton("Let's Play");
        btnPlay.setBounds(300, 350, 200, 45);
        bg.add(btnPlay);

        // ---------- BACK BUTTON ----------
        btnBack = new JButton("← Go Back");
        btnBack.setBounds(640, 20, 110, 30);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            this.dispose();
            levelScreen.setVisible(true);
        });

        btnPlay.addActionListener(e -> startGame());
    }

    private void startGame() {
        String p1 = txtPlayerA.getText().trim();
        String p2 = txtPlayerB.getText().trim();

        controller.gameController controller =
                new controller.gameController(
                        model.DifficultyLevel.valueOf(level),
                        p1, p2,
                        homeScreen
                );

        controller.startGame();
        this.setVisible(false);
    }

    // ------------------------------------------------------
    // 🔥 BACKGROUND PANEL (copy this everywhere you need a background)
    // ------------------------------------------------------
    private static class BackgroundPanel extends JPanel {

        private static final long serialVersionUID = 1L;
		private Image bg;

        public BackgroundPanel() {
            try {
                // Load from classpath
                bg = new ImageIcon(
                        getClass().getResource("/images/background.jpeg")
                ).getImage();
            } catch (Exception e) {
                // Fallback when running outside Eclipse
                bg = new ImageIcon("src/images/background.jpeg").getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
