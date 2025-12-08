package view;

import controller.gameController;
import model.board;
import model.game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class gameView extends JFrame {

    private static final long serialVersionUID = 1L;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;
    private JLabel scoreLabel;

    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private JButton btnExit;

    // FIXED: resources inside JAR
    private static final String BG_PATH = "/images/background.jpeg";
    private static final String HEART_FULL = "/images/live.png";
    private static final String HEART_EMPTY = "/images/Llive.png";

    public gameView(gameController controller, game model) {

        fullheart = loadIcon(HEART_FULL, 32, 32);
        emptyheart = loadIcon(HEART_EMPTY, 32, 32);

        setTitle("Dual Minesweeper Boards");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel(BG_PATH);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        btnExit = new JButton("Exit to Home");
        topPanel.add(btnExit);

        root.add(topPanel, BorderLayout.NORTH);

        btnExit.addActionListener(e -> controller.exitToHome());

        // boards
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 0));
        centerPanel.setOpaque(false);

        boardViews[0] = new boardView(0, "Player A", controller);
        boardViews[1] = new boardView(1, "Player B", controller);

        centerPanel.add(boardViews[0]);
        centerPanel.add(boardViews[1]);

        root.add(centerPanel, BorderLayout.CENTER);

        // hearts panel
        JPanel lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        lifePanel.setOpaque(false);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            heartLabels[i].setOpaque(false);
            lifePanel.add(heartLabels[i]);
        }

        // score
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        // bottom panel 
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(lifePanel, BorderLayout.CENTER);
        bottomPanel.add(scoreLabel, BorderLayout.EAST);

        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(1200, 700);
        //
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        setActiveBoard(0);
    }

    // methods

    public void setActiveBoard(int playerIndex) {
        boardViews[0].setActive(playerIndex == 0);
        boardViews[1].setActive(playerIndex == 1);
    }

    public void updateLives(int livesRemaining) {
        for (int i = 0; i < heartLabels.length; i++) {
            heartLabels[i].setIcon(i < livesRemaining ? fullheart : emptyheart);
        }
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    public void revealSafeCell(int boardIndex, int row, int col, int count) {
        boardViews[boardIndex].revealSafeCell(row, col, count);
    }

    public void revealMineHit(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealMineHit(row, col);
    }

    public void revealAllMines(int boardIndex, board model) {
        boardViews[boardIndex].revealAllMines(model);
    }

    public void updateTileFlag(int boardIndex, int row, int col) {
        boardViews[boardIndex].setFlagAtCell(row, col);
    }

    public void setFlagMode(int boardIndex, boolean on) {
        boardViews[boardIndex].setFlagMode(on);
    }

    public void showNotYourTurnMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Illegal move: it's not your turn!",
                "Not your turn",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public int showGameOverDialog() {
        return JOptionPane.showOptionDialog(
                this,
                "No lives remaining! Game Over!",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                "Play Again"
        );
    }

    public void showWinForBoth(int finalScore) {
        int choice = JOptionPane.showOptionDialog(
                this,
                "You won!\nFinal Score: " + finalScore + "\n\nPlay again?",
                "Victory",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                "Play Again"
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new controller.gameController();
        } else {
            System.exit(0);
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.out.println("❌ Missing image: " + path);
                return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
            }

            BufferedImage img = ImageIO.read(url);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception e) {
            System.out.println("❌ Error loading: " + path);
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }


    private static class BackgroundPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Image bg;

        BackgroundPanel(String path) {
            try {
                bg = new ImageIcon(
                        BackgroundPanel.class.getResource(path)
                ).getImage();
            } catch (Exception e) {
                bg = null;
            }
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
