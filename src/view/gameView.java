package view;

import controller.gameController;
import model.board;
import model.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class gameView extends JFrame {
	
	private static final long serialVersionUID = 1L;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;

    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private static final String BACKGROUND = "src/images/background.jpeg";
    private static final String HEART_FULL = "src/images/live.png";
    private static final String HEART_EMPTY = "src/images/llive.png";

    public gameView(gameController controller, game model) {

        fullheart = loadIcon(HEART_FULL, 32, 32);
        emptyheart = loadIcon(HEART_EMPTY, 32, 32);

        setTitle("Dual Minesweeper Boards");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel(BACKGROUND);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 70, 0));
        centerPanel.setOpaque(false);

        boardViews[0] = new boardView(0, "Player 1", controller);
        boardViews[1] = new boardView(1, "Player 2", controller);

        centerPanel.add(boardViews[0]);
        centerPanel.add(boardViews[1]);

        root.add(centerPanel, BorderLayout.CENTER);

        // Hearts panel
        JPanel lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {}
        };
        lifePanel.setOpaque(false);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            heartLabels[i].setOpaque(false);
            lifePanel.add(heartLabels[i]);
        }

        root.add(lifePanel, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(1200, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        setActiveBoard(0);
    }

    // ---------- PUBLIC METHODS FOR CONTROLLER ----------

    public void setActiveBoard(int playerIndex) {
        boardViews[0].setActive(playerIndex == 0);
        boardViews[1].setActive(playerIndex == 1);
    }

    public void updateLives(int livesRemaining) {
        for (int i = 0; i < heartLabels.length; i++) {
            heartLabels[i].setIcon(i < livesRemaining ? fullheart : emptyheart);
        }
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

    public void notYourTurn() {
        JOptionPane.showMessageDialog(this,
                "Illegal move: it's not your turn!",
                "Not your turn",
                JOptionPane.ERROR_MESSAGE);
    }

    public int gameOverDialog() {
        return JOptionPane.showOptionDialog(
                this,
                "No lives remaining! Game Over!",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                "Play Again");
    }

    // -------------- HELPERS ----------------

    private ImageIcon loadIcon(String path, int w, int h) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private static class BackgroundPanel extends JPanel {
        private Image bg;
        BackgroundPanel(String path) {
            try {
                bg = new ImageIcon(path).getImage();
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
