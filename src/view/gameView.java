package view;

import controller.gameController;
import model.board;
import model.game;
import model.DifficultyLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class gameView extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(gameView.class.getName());

    private JPanel motivationOverlay;
    private JLabel motivationLabel;
    private javax.swing.Timer motivationTimer;

    private final boardView[] boardViews = new boardView[2];
    private final JLabel[] heartLabels;
    private JLabel scoreLabel;

    // elapsed timer (existing)
    private JLabel timerLabel;
    private javax.swing.Timer gameTimer;
    private int elapsedSeconds = 0;

    // ===== TURN TIMER =====
    private JLabel turnTimerLabel;
    private javax.swing.Timer turnTimer;
    private int turnSecondsLeft = 0;

    private final ImageIcon fullheart;
    private final ImageIcon emptyheart;

    private JButton btnExit;

    private final gameController controller;
    private final game model;

    private static final String BG_PATH = "/images/background.jpeg";
    private static final String HEART_FULL = "/images/live.png";
    private static final String HEART_EMPTY = "/images/Llive.png";

    public gameView(gameController controller, game model) {

        this.controller = controller;
        this.model = model;

        fullheart = loadIcon(HEART_FULL, 32, 32);
        emptyheart = loadIcon(HEART_EMPTY, 32, 32);

        setTitle("Dual Minesweeper Boards");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BackgroundPanel root = new BackgroundPanel(BG_PATH);
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel speaker = SpeakerIcon.createSpeakerLabel();

        int iconSize = 40;
        int marginLeft = 10;
        int marginBottom = 5;

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                speaker.setBounds(
                        marginLeft,
                        getHeight() - iconSize - marginBottom,
                        iconSize,
                        iconSize
                );

                if (motivationOverlay != null) {
                    motivationOverlay.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });

        btnExit = new JButton("exit");
        btnExit.setFocusable(false);
        btnExit.addActionListener(e -> controller.exitToHome());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);
        topPanel.add(btnExit);

        // ===== message area (bottom) =====
        motivationLabel = new JLabel(" ");
        motivationLabel.setForeground(Color.WHITE);
        motivationLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        msgPanel.setOpaque(false);
        msgPanel.add(motivationLabel);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(topPanel, BorderLayout.NORTH);

        // ===== elapsed timer label =====
        timerLabel = new JLabel("Timer: 00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 26));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== turn timer label (NOW under timer) =====
        turnTimerLabel = new JLabel("Time left for your turn: --");
        turnTimerLabel.setForeground(Color.WHITE);
        turnTimerLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        turnTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Put both labels in one vertical box
        JPanel timerPanel = new JPanel();
        timerPanel.setOpaque(false);
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.add(timerLabel);
        timerPanel.add(Box.createVerticalStrut(6));
        timerPanel.add(turnTimerLabel);

        JPanel boardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        boardsPanel.setOpaque(false);

        int tileSize = computeTileSizeForBoards();

        boardViews[0] = new boardView(0, model.getPlayer1Name(), controller, tileSize);
        boardViews[1] = new boardView(1, model.getPlayer2Name(), controller, tileSize);

        boardsPanel.add(boardViews[0]);
        boardsPanel.add(boardViews[1]);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(timerPanel, BorderLayout.NORTH);
        centerPanel.add(boardsPanel, BorderLayout.CENTER);

        content.add(centerPanel, BorderLayout.CENTER);

        JPanel lifePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        lifePanel.setOpaque(false);

        heartLabels = new JLabel[model.getMaxLives()];
        for (int i = 0; i < model.getMaxLives(); i++) {
            heartLabels[i] = new JLabel(fullheart);
            lifePanel.add(heartLabels[i]);
        }
        updateLives(model.getLivesRemaining());

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Verdana", Font.BOLD, 22));

        // RIGHT SIDE: score only
        JPanel scoreAndTurnPanel = new JPanel();
        scoreAndTurnPanel.setOpaque(false);
        scoreAndTurnPanel.setLayout(new BoxLayout(scoreAndTurnPanel, BoxLayout.Y_AXIS));
        scoreLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        scoreAndTurnPanel.add(scoreLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        bottomPanel.add(msgPanel, BorderLayout.NORTH);
        bottomPanel.add(lifePanel, BorderLayout.CENTER);
        bottomPanel.add(scoreAndTurnPanel, BorderLayout.EAST);

        content.add(bottomPanel, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLocationRelativeTo(null);

        // ===== big motivation overlay label =====
        motivationLabel = new JLabel("", SwingConstants.CENTER);
        motivationLabel.setForeground(new Color(212, 175, 55));
        motivationLabel.setFont(new Font("Serif", Font.BOLD, 52));
        motivationLabel.setVisible(false);

        motivationOverlay = new JPanel(new GridBagLayout());
        motivationOverlay.setOpaque(false);
        motivationOverlay.add(motivationLabel);
        motivationOverlay.setVisible(false);

        getLayeredPane().add(motivationOverlay, JLayeredPane.POPUP_LAYER);
        getLayeredPane().add(speaker, JLayeredPane.PALETTE_LAYER);

        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            speaker.setBounds(
                    marginLeft,
                    getHeight() - iconSize - marginBottom,
                    iconSize,
                    iconSize
            );

            motivationOverlay.setBounds(0, 0, getWidth(), getHeight());
        });

        setActiveBoard(0);

        startTimer();
    }

    // ===================== Motivation overlay =====================
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

    // ===================== elapsed timer =====================
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

    // ===================== TURN TIMER =====================
    public void startTurnTimer(int seconds, Runnable onTimeout) {
        stopTurnTimer();
        turnSecondsLeft = seconds;
        turnTimerLabel.setText("Time left for your turn: " + turnSecondsLeft + "s");

        turnTimer = new javax.swing.Timer(1000, e -> {
            turnSecondsLeft--;
            turnTimerLabel.setText("Time left for your turn: " + turnSecondsLeft + "s");

            if (turnSecondsLeft <= 0) {
                stopTurnTimer();
                onTimeout.run();
            }
        });
        turnTimer.start();
    }

    public void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }

    // ===================== API used by controller =====================
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

    public void revealAllSurprises(int boardIndex, board model) {
        boardViews[boardIndex].revealAllSurprises(model);
    }

    public void updateTileFlag(int boardIndex, int row, int col) {
        boardViews[boardIndex].setFlagAtCell(row, col);
    }

    public void setFlagMode(int boardIndex, boolean on) {
        boardViews[boardIndex].setFlagMode(on);
    }

    public void setFlagAtCell(int boardIndex, int row, int col) {
        boardViews[boardIndex].setFlagAtCell(row, col);
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

    // ===================== dialogs =====================
    public int showGameOverDialog() {
        stopTimer();
        stopTurnTimer();
        return JOptionPane.showConfirmDialog(
                this,
                "Game Over!",
                "Game Over",
                JOptionPane.YES_NO_OPTION
        );
    }

    public void showWinForBoth(int finalScore) {
        stopTimer();
        stopTurnTimer();
        JOptionPane.showMessageDialog(this, "You win! Final Score: " + finalScore, "Victory", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== util =====================
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getFormattedElapsedTime() {
        return formatTime(elapsedSeconds);
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

    private int computeTileSizeForBoards() {

        Dimension window = getContentPane().getSize();

        if (window.width == 0 || window.height == 0) {
            window = Toolkit.getDefaultToolkit().getScreenSize();
        }

        int reservedWidth = 260;
        int reservedHeight = 320;

        int rows = model.getBoard(0).getRows();
        int cols = model.getBoard(0).getCols();

        int maxBoardWidth = (window.width - reservedWidth) / 2;
        int maxBoardHeight = window.height - reservedHeight;

        int tileByWidth = maxBoardWidth / cols;
        int tileByHeight = maxBoardHeight / rows;

        int tile = Math.min(tileByWidth, tileByHeight);

        tile = Math.max(tile, 22);
        tile = Math.min(tile, 46);

        return tile;
    }

    public boardView getBoardView(int i) {
        return boardViews[i];
    }
}
