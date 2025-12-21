package view;

import controller.gameController;
import model.board;
import model.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class gameView extends JFrame {
	
	
    private static final long serialVersionUID = 1L;

    // ===== Motivation Overlay (CENTER, GOLD, BIG) =====
    private JPanel motivationOverlay;
    private JLabel motivationLabel;
    private javax.swing.Timer motivationTimer;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;
    private JLabel scoreLabel;

    // timer
    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    private int elapsedSeconds = 0;

    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private JButton btnExit;

    private static final String BG_PATH = "/images/background.jpeg";
    private static final String HEART_FULL = "/images/live.png";
    private static final String HEART_EMPTY = "/images/Llive.png";

    public gameView(gameController controller, game model) {

        fullheart = loadIcon(HEART_FULL, 32, 32);
        emptyheart = loadIcon(HEART_EMPTY, 32, 32);

        setTitle("Dual Minesweeper Boards");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ===== Root background panel =====
        BackgroundPanel root = new BackgroundPanel(BG_PATH);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== Speaker icon (bottom-left) =====
        JLabel speaker = SpeakerIcon.createSpeakerLabel();
        root.setLayout(null); // needed because you use setBounds for speaker
        // We'll later add a transparent content panel above it
        root.add(speaker);

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 5;

        // keep bottom-left on resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setBounds(
                        marginLeft,
                        root.getHeight() - iconSize - marginBottom,
                        iconSize,
                        iconSize
                );

                if (motivationOverlay != null) {
                    motivationOverlay.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });


        // ===== TOP PANEL (Exit button) =====
        btnExit = new JButton("Exit");
        btnExit.setFocusable(false);
        btnExit.addActionListener(e -> controller.exitToHome());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);
        topPanel.add(btnExit);
        topPanel.setBounds(0, 0, 300, 60);
        root.add(topPanel);
        
        motivationLabel = new JLabel(" ");
        motivationLabel.setForeground(Color.WHITE);
        motivationLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        msgPanel.setOpaque(false);
        msgPanel.add(motivationLabel);
/*
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(lifePanel, BorderLayout.CENTER);
        bottomPanel.add(scoreLabel, BorderLayout.EAST);

        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(msgPanel, BorderLayout.NORTH);
        bottomWrapper.add(bottomPanel, BorderLayout.SOUTH);*/
      //  root.add(bottomWrapper, BorderLayout.SOUTH);

        // ===== Main transparent content panel (so BorderLayout still works) =====
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBounds(0, 0, 1920, 1080); // will be updated after visible
        root.add(content);


     /*   // ===== Exit button (if you actually want it on screen) =====
        btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> controller.exitToHome());
        btnExit.setFocusable(false);
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setOpaque(false);
        topPanel.add(btnExit);

        root.add(topPanel, BorderLayout.NORTH);*/


        // ===== Timer =====
        timerLabel = new JLabel("Timer: 00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 26));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timerPanel.setOpaque(false);
        timerPanel.add(timerLabel);

        // ===== Boards =====
        JPanel boardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 0));
        boardsPanel.setOpaque(false);

        boardViews[0] = new boardView(0, model.getPlayer1Name(), controller);
        boardViews[1] = new boardView(1, model.getPlayer2Name(), controller);

        // DEBUG ONLY (optional)
        // boardViews[0].debugRevealAllSurprises(model.getBoard(0));
        // boardViews[1].debugRevealAllSurprises(model.getBoard(1));

        boardsPanel.add(boardViews[0]);
        boardsPanel.add(boardViews[1]);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(timerPanel, BorderLayout.NORTH);
        centerPanel.add(boardsPanel, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        // ===== Life panel =====
        JPanel lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        lifePanel.setOpaque(false);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            lifePanel.add(heartLabels[i]);
        }
        updateLives(model.getLivesRemaining());

        // ===== Score =====
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        // ===== Bottom panel (lives + score + exit) =====
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        bottomPanel.add(msgPanel, BorderLayout.NORTH);

        bottomPanel.add(lifePanel, BorderLayout.CENTER);
        bottomPanel.add(scoreLabel, BorderLayout.EAST);

       /* JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.setOpaque(false);
        leftBottom.add(btnExit);
        bottomPanel.add(leftBottom, BorderLayout.WEST);*/

        content.add(bottomPanel, BorderLayout.SOUTH);

        // ===== Set content pane =====
        setContentPane(root);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        // ===== Motivation Overlay (CENTER, GOLD, BIG) =====
        motivationLabel = new JLabel("", SwingConstants.CENTER);
        motivationLabel.setForeground(new Color(212, 175, 55)); // GOLD
        motivationLabel.setFont(new Font("Serif", Font.BOLD, 52));
        motivationLabel.setVisible(false);

        motivationOverlay = new JPanel(new GridBagLayout());
        motivationOverlay.setOpaque(false);
        motivationOverlay.add(motivationLabel);
        motivationOverlay.setVisible(false);

        // place overlay above everything
        getLayeredPane().add(motivationOverlay, JLayeredPane.POPUP_LAYER);

        // after frame is visible, fix bounds for speaker/content/overlay
        SwingUtilities.invokeLater(() -> {
            content.setBounds(0, 0, root.getWidth(), root.getHeight());

            speaker.setBounds(
                    marginLeft,
                    root.getHeight() - iconSize - marginBottom,
                    iconSize,
                    iconSize
            );

            motivationOverlay.setBounds(0, 0, getWidth(), getHeight());
        });

        setActiveBoard(0);

        // start timer automatically
        startTimer();
    }

    // ======================= Motivation message (center overlay) =======================
    public void showMotivationMessage(String msg) {
        if (msg == null || msg.isBlank()) return;

        motivationLabel.setText(msg);
        motivationLabel.setVisible(true);
        motivationOverlay.setVisible(true);

        if (motivationTimer != null && motivationTimer.isRunning()) {
            motivationTimer.stop();
        }

        motivationTimer = new javax.swing.Timer(2500, e -> {
            motivationLabel.setVisible(false);
            motivationOverlay.setVisible(false);
        });
        motivationTimer.setRepeats(false);
        motivationTimer.start();
    }

    // ======================= Public API used by controller =======================
    public void revealAllSurprises(int boardIndex, board model) {
        boardViews[boardIndex].revealAllSurprises(model);
    }

    public void startTimer() {
        stopTimer();
        elapsedSeconds = 0;
        timerLabel.setText("Timer: 00:00");

        gameTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Timer: " + formatTime(elapsedSeconds));
        });
        gameTimer.start();
    }

    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

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

    public void revealSurprise(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealSurprise(row, col);
    }

    public void activateSurprise(int boardIndex, int row, int col) {
        boardViews[boardIndex].activateSurprise(row, col);
    }

    public void removeFlag(int boardIndex, int row, int col) {
        boardViews[boardIndex].removeFlag(row, col);
    }

    public void showRemoveFlagMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Remove the flag to reveal the cell",
                "Flagged Cell",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    //question cell
    public void revealMineAuto(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealMineAuto(row, col);
    }
    public void revealHintCell(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealHintCell(row, col);
    }

    public void revealQuestion(int boardIndex, int row, int col) {
        boardViews[boardIndex].revealQuestion(row, col);
    }
    
    public void markQuestionUsed(int boardIndex, int row, int col) {
        boardViews[boardIndex].markQuestionUsed(row, col);
    }

    public int showGameOverDialog() {
        stopTimer();
        return JOptionPane.showOptionDialog(
                this,
                "No lives remaining!\nTimer: " + formatTime(elapsedSeconds),
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Play Again", "Exit"},
                "Play Again"
        );
    }

    public void showWinForBoth(int finalScore) {
        stopTimer();
        int choice = JOptionPane.showOptionDialog(
                this,
                "You won!\nFinal Score: " + finalScore +
                        "\nTimer: " + formatTime(elapsedSeconds),
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
            var url = getClass().getResource(path);
            if (url == null)
                return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));

            BufferedImage img = ImageIO.read(url);
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }
    }

    public void showSurpriseResult(boolean good,
                                  int lifeDelta,
                                  int rewardPoints,
                                  int activationCost,
                                  int fullLifePenalty,
                                  int netPoints) {

        StringBuilder msg = new StringBuilder();

        if (good) {
            msg.append("🎉 Lucky you!\nGood surprise!\n");
            msg.append("Reward: +").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");

            if (lifeDelta == 1) {
                msg.append("Lives: +1 ❤️\n");
            } else {
                msg.append("Lives were full → extra cost: -")
                        .append(fullLifePenalty)
                        .append(" points (for the extra life)\n");
            }

        } else {
            msg.append("😬 Oops!\nBad surprise!\n");
            msg.append("Effect: ").append(rewardPoints).append(" points\n");
            msg.append("Activation cost: -").append(activationCost).append(" points\n");
            msg.append("Lives: -1 ❤️\n");
        }

        msg.append("\nNet change: ").append(netPoints >= 0 ? "+" : "").append(netPoints).append(" points");

        JOptionPane.showMessageDialog(this, msg.toString(), "Surprise!", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image bg;

        BackgroundPanel(String path) {
            try {
                bg = new ImageIcon(BackgroundPanel.class.getResource(path)).getImage();
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

   /* 
    public void showMotivationMessage(String msg) {
        if (msg == null || msg.isBlank()) return;

        motivationLabel.setText(msg);

        // clear after 2.5 seconds
        if (motivationTimer != null && motivationTimer.isRunning()) {
            motivationTimer.stop();
        }

        motivationTimer = new javax.swing.Timer(2500, e -> motivationLabel.setText(" "));
        motivationTimer.setRepeats(false);
        motivationTimer.start();
    }*/
    
    public String getFormattedElapsedTime() {
        return formatTime(elapsedSeconds);
    }
}
