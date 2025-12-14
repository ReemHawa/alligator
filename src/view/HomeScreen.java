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
    	
    	music.bcMusic.play("/music/Host Entrance Background Music.wav");

        setTitle("MineSweeper - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        setSize(800, 550);
        setLocationRelativeTo(null);

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

        btnViewHistory = new JButton("View Games History");
        btnViewHistory.setBounds(270, 190, 260, 40);
        bg.add(btnViewHistory);

        btnViewQuestions = new JButton("View Questions Management");
        btnViewQuestions.setBounds(270, 250, 260, 40);
        bg.add(btnViewQuestions);

        btnStartNewGame = new JButton("Start A New Game");
        btnStartNewGame.setBounds(270, 310, 260, 40);
        bg.add(btnStartNewGame);

        new homeScreenController(this);

        setVisible(true);
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
                // Fallback when classpath fails (standalone mode)
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
